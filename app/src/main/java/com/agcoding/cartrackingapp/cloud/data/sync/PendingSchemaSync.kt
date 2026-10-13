package com.agcoding.cartrackingapp.cloud.data.sync

import com.agcoding.cartrackingapp.cloud.data.local.SyncLinkDao
import com.agcoding.cartrackingapp.cloud.domain.model.SyncProgress
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStatus
import com.agcoding.cartrackingapp.cloud.domain.model.SyncSummary
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The sync engine before there is anything to sync.
 *
 * The account half of the backend is finished and live: signing up, signing
 * in, the emailed temporary password, changing the password, signing out and
 * deleting the account all talk to the real Supabase project. The data half is
 * not, because the `cars`, `fuel_refills`, `expenses`, `trips` and
 * `expense_categories` tables cannot be written until two product questions
 * are answered — whether an item in the trash should appear in the trash on
 * the other device, and whether a car can ever belong to more than one
 * account. Both change the columns and every row-level security policy.
 *
 * So this reports the truth, which is that nothing moves. In particular it
 * does **not** touch `lastSyncAt` or `hasPendingChanges`: those two are what
 * [com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository.signOut]
 * consults before it is willing to wipe the phone, and claiming a sync that
 * never happened is exactly how data gets lost.
 *
 * Replacing it with the real SyncManager is one line in
 * [com.agcoding.cartrackingapp.cloud.di.CloudModule]. Nothing else changes:
 * every caller already goes through [CloudSync].
 */
@Singleton
class PendingSchemaSync @Inject constructor(
    private val syncLinkDao: SyncLinkDao,
) : CloudSync {

    override val movesData: Boolean = false

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()

    private val _progress = MutableStateFlow<SyncProgress?>(null)
    override val progress: StateFlow<SyncProgress?> = _progress.asStateFlow()

    override fun requestSync(localChange: Boolean) = Unit

    override suspend fun syncNow(): SyncSummary = SyncSummary()

    /**
     * Real work even now: the table exists, and an account that signs out must
     * not leave its links behind for the next one to trip over.
     */
    override suspend fun reset() = syncLinkDao.clear()
}
