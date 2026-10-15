package com.agcoding.cartrackingapp.cloud.data.local

import android.content.Context
import androidx.room.withTransaction
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Removes every trace of the account's data from this phone.
 *
 * Three things call it: signing out, deleting the account, and "Clear all
 * data" in Settings — which is only offered when signed out, because while
 * signed in what the phone holds is a copy of the account rather than its own
 * records.
 *
 * Clearing `sync_links` in the same transaction as the data is the part that
 * matters, and the reason everything goes through here. A wipe that leaves
 * the links behind looks to the next sync like "the user deleted all of
 * this", and the deletions are pushed — which is how clearing one phone used
 * to destroy the server copy and, through it, every other device. With the
 * links gone the server rows are simply unlinked, so the next sync treats
 * them as rows it has not seen and brings them back.
 *
 * The settings are deliberately untouched: language, theme and the colour
 * palette belong to the device, not to the account.
 */
@Singleton
class LocalDataWiper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CarDatabase,
) {

    suspend fun wipe(): Unit = withContext(Dispatchers.IO) {
        // Children before parents. The foreign keys cascade anyway, but
        // spelling the order out means this keeps working if one of them is
        // ever changed to RESTRICT.
        database.withTransaction {
            database.carAttachmentDao().deleteAll()
            database.expenseDao().deleteAll()
            database.fuelRefillDao().deleteAll()
            database.tripDao().deleteAll()
            database.carDao().deleteAll()
            database.expenseCategoryDao().deleteAll()
            database.notificationHistoryDao().deleteAll()
            database.syncLinkDao().clear()
        }

        // The attachment rows are gone; without this their files would sit in
        // internal storage forever, invisible and unreachable.
        File(context.filesDir, ATTACHMENTS_DIR).deleteRecursively()
    }

    private companion object {
        /** Must match CarAttachmentRepositoryImpl.ATTACHMENTS_DIR. */
        const val ATTACHMENTS_DIR = "attachments"
    }
}
