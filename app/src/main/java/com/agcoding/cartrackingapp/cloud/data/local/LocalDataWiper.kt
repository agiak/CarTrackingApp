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
 * Only two things ever call this: deleting the account, where the data is gone
 * from the server too, and signing out when the user explicitly asks for the
 * phone to be left clean. It is never called implicitly — this app works
 * perfectly well with no account at all, so losing the data by signing out of
 * something optional would be an unpleasant surprise.
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
