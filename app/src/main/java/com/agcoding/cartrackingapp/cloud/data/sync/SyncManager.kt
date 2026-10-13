package com.agcoding.cartrackingapp.cloud.data.sync

import androidx.room.InvalidationTracker
import androidx.room.withTransaction
import com.agcoding.cartrackingapp.cloud.data.local.SyncLinkDao
import com.agcoding.cartrackingapp.cloud.data.local.SyncLinkEntity
import com.agcoding.cartrackingapp.cloud.data.network.NetworkMonitor
import com.agcoding.cartrackingapp.cloud.data.remote.RemoteRows
import com.agcoding.cartrackingapp.cloud.data.remote.RemoteTable
import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseClient
import com.agcoding.cartrackingapp.cloud.data.remote.toCloudException
import com.agcoding.cartrackingapp.cloud.data.session.CloudSessionProvider
import com.agcoding.cartrackingapp.cloud.data.session.CloudSessionStore
import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import com.agcoding.cartrackingapp.cloud.domain.model.SyncProgress
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStage
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStatus
import com.agcoding.cartrackingapp.cloud.domain.model.SyncSummary
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import com.agcoding.cartrackingapp.cloud.domain.sync.LocalItem
import com.agcoding.cartrackingapp.cloud.domain.sync.RemoteItem
import com.agcoding.cartrackingapp.cloud.domain.sync.SyncLink
import com.agcoding.cartrackingapp.cloud.domain.sync.hashOf
import com.agcoding.cartrackingapp.cloud.domain.sync.planSync
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Two-way sync between this phone's database and the user's rows on Supabase.
 *
 * Every sync pulls all of the user's rows, merges each table with
 * [planSync] against the links saved last time, pushes what changed here and
 * applies what changed elsewhere. There is no incremental "give me everything
 * since X": a car history is small enough that a full compare is cheaper to
 * get right than a cursor is, and a full compare cannot drift.
 *
 * Tables go in dependency order — cars, then trips, then refills, then
 * expenses — because a child row carries its parent's **remote** id, and that
 * id only exists once the parent's own table has synced.
 */
@Singleton
class SyncManager @Inject constructor(
    private val client: SupabaseClient,
    private val store: CloudSessionStore,
    private val sessions: CloudSessionProvider,
    private val networkMonitor: NetworkMonitor,
    private val database: CarDatabase,
    private val linkDao: SyncLinkDao,
) : CloudSync {

    override val movesData: Boolean = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()

    private val _progress = MutableStateFlow<SyncProgress?>(null)
    override val progress: StateFlow<SyncProgress?> = _progress.asStateFlow()

    /** One sync at a time, and never while the local data is being wiped. */
    private val mutex = Mutex()

    /**
     * Bumped on every local change, so a sync only reports clean the edits it
     * actually uploaded. Without it, something saved mid-sync would be marked
     * as sent and then sit on the phone until the next unrelated change.
     */
    private val changeVersion = AtomicLong(0)

    private var pending: Job? = null

    private val carDao get() = database.carDao()
    private val tripDao get() = database.tripDao()
    private val refillDao get() = database.fuelRefillDao()
    private val expenseDao get() = database.expenseDao()
    private val categoryDao get() = database.expenseCategoryDao()

    /**
     * Local writes are ignored until this moment. Set while a sync is applying
     * what it received, because those writes are not news — reacting to them
     * would have the sync endlessly re-triggering itself.
     */
    @Volatile
    private var suppressWritesUntil = 0L

    init {
        // Back online: send what was changed meanwhile and pick up the other
        // phone's changes. drop(1) skips the value the flow already holds, so
        // launching while online does not fire a redundant sync on top of the
        // one MainActivity asks for.
        scope.launch {
            networkMonitor.isOnline.drop(1).filter { it }.collect { requestSync() }
        }

        // Every change to a synced table asks for a sync, wherever it came
        // from. Watching the database rather than hooking each repository is
        // deliberate: several screens write through their DAO directly, so
        // per-repository hooks would silently miss them — and the next one
        // somebody writes would be missed too.
        database.invalidationTracker.addObserver(
            object : InvalidationTracker.Observer(SYNCED_TABLES) {
                override fun onInvalidated(tables: Set<String>) {
                    if (System.currentTimeMillis() >= suppressWritesUntil) {
                        requestSync(localChange = true)
                    }
                }
            }
        )
    }

    override fun requestSync(localChange: Boolean) {
        if (localChange) {
            changeVersion.incrementAndGet()
            scope.launch { store.savePendingChanges(true) }
        }
        // Saving a car writes several rows in a row; without this each one
        // would start its own upload.
        pending?.cancel()
        pending = scope.launch {
            delay(DEBOUNCE_MILLIS)
            if (networkMonitor.isOnline.value && sessions.valid() != null) {
                runCatching { syncNow() }
            }
        }
    }

    override suspend fun syncNow(): SyncSummary = withContext(Dispatchers.IO) {
        mutex.withLock {
            _status.value = SyncStatus.Syncing
            val version = changeVersion.get()
            suppressWritesUntil = Long.MAX_VALUE
            try {
                val summary = sync()
                _status.value = SyncStatus.Idle
                store.saveLastSyncAt(System.currentTimeMillis())
                if (changeVersion.get() == version) {
                    store.savePendingChanges(false)
                } else {
                    // Something was saved while this was running; it has not
                    // been uploaded, so it stays pending and goes next round.
                    requestSync()
                }
                summary
            } catch (e: Exception) {
                _status.value = SyncStatus.Failed(e.message ?: e.javaClass.simpleName)
                throw e.toCloudException()
            } finally {
                // Room reports an invalidation a moment after the transaction
                // that caused it, so the window stays open briefly past the
                // end of the sync to catch its own trailing notifications.
                suppressWritesUntil = System.currentTimeMillis() + SETTLE_MILLIS
            }
        }
    }

    override suspend fun reset() {
        mutex.withLock {
            linkDao.clear()
            _progress.value = null
            _status.value = SyncStatus.Idle
        }
    }

    // ── one round ─────────────────────────────────────────────────────────

    private suspend fun sync(): SyncSummary {
        report(SyncStage.CONNECTING, 0.02f)
        val session = sessions.require()

        // Links built for a different account point at that account's rows.
        if (store.current().linksOwnerId != session.userId) {
            linkDao.clear()
            store.saveLinksOwnerId(session.userId)
        }

        report(SyncStage.CHECKING, 0.05f)
        val remote = RemoteTable.entries.associateWith { table ->
            client.selectAll(session, table.tableName, table.orderBy)
                .map { RemoteRows.parse(table, it) }
        }
        report(SyncStage.CHECKING, 0.10f)

        // ── cars ──────────────────────────────────────────────────────────
        val cars = syncTable(
            table = RemoteTable.CARS,
            session = session,
            remote = remote.getValue(RemoteTable.CARS),
            local = carDao.getAllForSync().map { it.toLocalItem() },
            newRemoteId = { UUID.randomUUID().toString() },
            insert = { row -> carDao.insertCar(row.toCar(id = 0)).toString() },
            update = { key, row -> carDao.updateCar(row.toCar(id = key.toLong())); true },
            delete = { key -> carDao.deleteCarById(key.toLong()) },
            stage = SyncStage.CARS, from = 0.10f, to = 0.30f,
        )

        val carToRemote = idMap(RemoteTable.CARS)
        val carToLocal = carToRemote.entries.associate { (local, remote) -> remote to local }

        // ── trips ─────────────────────────────────────────────────────────
        val trips = syncTable(
            table = RemoteTable.TRIPS,
            session = session,
            remote = remote.getValue(RemoteTable.TRIPS),
            local = tripDao.getAllForSync().mapNotNull { it.toLocalItem(carToRemote) },
            newRemoteId = { UUID.randomUUID().toString() },
            insert = { row -> row.toTrip(id = 0, carToLocal)?.let { tripDao.insertTrip(it).toString() } },
            update = { key, row ->
                val trip = row.toTrip(key.toLong(), carToLocal)
                if (trip != null) tripDao.updateTrip(trip)
                trip != null
            },
            delete = { key -> tripDao.deleteTripById(key.toLong()) },
            stage = SyncStage.TRIPS, from = 0.30f, to = 0.42f,
        )

        val tripToRemote = idMap(RemoteTable.TRIPS)
        val tripToLocal = tripToRemote.entries.associate { (local, remote) -> remote to local }

        // ── refills ───────────────────────────────────────────────────────
        val refills = syncTable(
            table = RemoteTable.REFILLS,
            session = session,
            remote = remote.getValue(RemoteTable.REFILLS),
            local = refillDao.getAllForSync().mapNotNull { it.toLocalItem(carToRemote, tripToRemote) },
            newRemoteId = { UUID.randomUUID().toString() },
            insert = { row ->
                row.toRefill(id = 0, carToLocal, tripToLocal)?.let { refillDao.insertRefill(it).toString() }
            },
            update = { key, row ->
                val refill = row.toRefill(key.toLong(), carToLocal, tripToLocal)
                if (refill != null) refillDao.updateRefill(refill)
                refill != null
            },
            delete = { key -> refillDao.deleteRefillById(key.toLong()) },
            stage = SyncStage.REFILLS, from = 0.42f, to = 0.70f,
        )

        // ── expenses ──────────────────────────────────────────────────────
        val expenses = syncTable(
            table = RemoteTable.EXPENSES,
            session = session,
            remote = remote.getValue(RemoteTable.EXPENSES),
            local = expenseDao.getAllForSync().mapNotNull { it.toLocalItem(carToRemote) },
            newRemoteId = { UUID.randomUUID().toString() },
            insert = { row ->
                row.toExpense(id = 0, carToLocal)?.let { expenseDao.insertExpense(it).toString() }
            },
            update = { key, row ->
                val expense = row.toExpense(key.toLong(), carToLocal)
                if (expense != null) expenseDao.updateExpense(expense)
                expense != null
            },
            delete = { key -> expenseDao.deleteExpenseById(key.toLong()) },
            stage = SyncStage.EXPENSES, from = 0.70f, to = 0.90f,
        )

        // ── categories ────────────────────────────────────────────────────
        // Keyed by name on both sides, so a row that is already here is the
        // same row rather than a duplicate to insert.
        val categories = syncTable(
            table = RemoteTable.CATEGORIES,
            session = session,
            remote = remote.getValue(RemoteTable.CATEGORIES),
            local = categoryDao.getAllForSync().map { it.toLocalItem() },
            newRemoteId = { it.localKey },
            insert = { row ->
                val name = row.remoteId
                val existing = categoryDao.getCategoryByName(name)
                if (existing == null) {
                    categoryDao.insertCategory(row.toCategory(id = 0, name = name))
                } else {
                    categoryDao.updateCategory(row.toCategory(id = existing.id, name = name))
                }
                name
            },
            update = { key, row ->
                val existing = categoryDao.getCategoryByName(key)
                if (existing != null) {
                    categoryDao.updateCategory(row.toCategory(id = existing.id, name = key))
                }
                existing != null
            },
            delete = { key -> categoryDao.deleteCategoryByNameForSync(key) },
            stage = SyncStage.CATEGORIES, from = 0.90f, to = 0.96f,
        )

        report(SyncStage.FINISHING, 0.98f)
        val summary = SyncSummary(
            cars = cars.pushed,
            trips = trips.pushed,
            refills = refills.pushed,
            expenses = expenses.pushed,
            categories = categories.pushed,
            received = cars.received + trips.received + refills.received +
                expenses.received + categories.received,
        )
        report(SyncStage.DONE, 1f)
        return summary
    }

    private data class TableResult(val pushed: Int = 0, val received: Int = 0)

    /**
     * Merges one table and carries out the plan.
     *
     * @param insert applies a row that is new here; returns its new local key,
     *   or null when it cannot be applied yet (its car is not here).
     * @param update overwrites a local row; false when it cannot be applied.
     */
    private suspend fun syncTable(
        table: RemoteTable,
        session: CloudSession,
        remote: List<RemoteItem>,
        local: List<LocalItem>,
        newRemoteId: (LocalItem) -> String,
        insert: suspend (RemoteItem) -> String?,
        update: suspend (String, RemoteItem) -> Boolean,
        delete: suspend (String) -> Unit,
        stage: SyncStage,
        from: Float,
        to: Float,
    ): TableResult {
        report(stage, from)

        val oldLinks = linkDao.getAll(table.name)
            .map { SyncLink(it.localKey, it.remoteId, it.hash) }
        val plan = planSync(local, remote, oldLinks, newRemoteId)
        if (plan.isEmpty) {
            report(stage, to)
            return TableResult()
        }

        // Remember the ids of brand-new rows before pushing them, so a crash
        // halfway through an upload reuses them on the retry instead of
        // minting a second set and duplicating everything.
        val knownKeys = oldLinks.map { it.localKey }.toSet()
        val fresh = plan.links.filter { it.localKey !in knownKeys }
        if (fresh.isNotEmpty()) {
            linkDao.upsertAll(fresh.map { it.toEntity(table, hash = "") })
        }

        // Uploading is what takes the time, so it gets most of this table's
        // share of the bar; the rest snaps forward when the local work is done.
        val uploadSpan = (to - from) * 0.9f
        if (plan.push.isNotEmpty()) {
            client.upsert(
                session = session,
                table = table.tableName,
                rows = plan.push.map { (remoteId, fields) ->
                    RemoteRows.toJson(table, session.userId, remoteId, fields)
                },
                onConflict = table.conflictColumns,
                onProgress = { done, total -> report(stage, from + uploadSpan * done / total) },
            )
        }
        if (plan.softDelete.isNotEmpty()) {
            client.softDelete(
                session = session,
                table = table.tableName,
                keyColumn = table.keyColumn,
                ids = plan.softDelete,
                deletedAt = Instant.now().toString(),
            )
        }

        val finalLinks = plan.links.associateBy { it.localKey }.toMutableMap()
        var received = 0
        database.withTransaction {
            plan.deleteLocal.forEach { delete(it) }
            plan.updateLocal.forEach { (key, row) ->
                if (update(key, row)) {
                    received++
                } else {
                    // Could not be applied — its car has not arrived yet.
                    // Keeping the old link means it is retried next sync
                    // rather than quietly losing its place.
                    oldLinks.find { it.localKey == key }?.let { finalLinks[key] = it }
                }
            }
            plan.insertLocal.forEach { row ->
                insert(row)?.let { key ->
                    finalLinks[key] = SyncLink(key, row.remoteId, hashOf(row.fields))
                    received++
                }
            }
            linkDao.deleteAll(table.name)
            linkDao.upsertAll(finalLinks.values.map { it.toEntity(table, it.hash) })
        }

        report(stage, to)
        return TableResult(pushed = plan.push.size, received = received)
    }

    /** localKey (as Long) → remote id, for translating a child's parent. */
    private suspend fun idMap(table: RemoteTable): Map<Long, String> =
        linkDao.getAll(table.name).mapNotNull { link ->
            link.localKey.toLongOrNull()?.let { it to link.remoteId }
        }.toMap()

    private fun report(stage: SyncStage, fraction: Float) {
        _progress.value = SyncProgress(stage, fraction.coerceIn(0f, 1f))
    }

    private fun SyncLink.toEntity(table: RemoteTable, hash: String) =
        SyncLinkEntity(entity = table.name, localKey = localKey, remoteId = remoteId, hash = hash)

    private companion object {
        /** Long enough to collapse one screen's worth of saves into one sync. */
        const val DEBOUNCE_MILLIS = 1_500L

        /** How long after a sync its own database writes are still ignored. */
        const val SETTLE_MILLIS = 2_000L

        val SYNCED_TABLES = arrayOf(
            "cars", "trips", "fuel_refills", "expenses", "expense_categories",
        )
    }
}
