package com.agcoding.cartrackingapp.cloud.domain.sync

import java.security.MessageDigest

/** One row as it exists on the phone, already normalised by [SyncFields]. */
data class LocalItem(val localKey: String, val fields: Map<String, String?>)

/** One row as it exists on the server, normalised the same way. */
data class RemoteItem(
    val remoteId: String,
    val fields: Map<String, String?>,
    val deleted: Boolean = false,
)

/**
 * What the last successful sync saw: which local row is which remote row, and
 * what the contents hashed to at that moment. The hash is the whole trick —
 * it is what lets the merge tell "I changed this" from "they changed this"
 * without trusting two devices' clocks to agree.
 */
data class SyncLink(val localKey: String, val remoteId: String, val hash: String)

data class EntityPlan(
    /** remoteId → fields to write. */
    val push: Map<String, Map<String, String?>>,
    val softDelete: List<String>,
    val insertLocal: List<RemoteItem>,
    /** localKey → the remote row to overwrite it with. */
    val updateLocal: Map<String, RemoteItem>,
    val deleteLocal: List<String>,
    val links: List<SyncLink>,
) {
    val isEmpty: Boolean
        get() = push.isEmpty() && softDelete.isEmpty() && insertLocal.isEmpty() &&
            updateLocal.isEmpty() && deleteLocal.isEmpty()
}

/**
 * A stable fingerprint of a row's contents.
 *
 * Sorted so key order cannot matter, and joined with control characters that
 * cannot occur in the values themselves — otherwise `{a: "x", b: "y"}` and
 * `{a: "x=b=y"}` would collide. A null is distinct from an empty string: the
 * difference between "no note" and "a note the user cleared" is real.
 */
fun hashOf(fields: Map<String, String?>): String {
    val canonical = fields.toSortedMap().entries
        .joinToString("\u001F") { (key, value) -> "$key=${value ?: "\u0000"}" }
    return MessageDigest.getInstance("SHA-256")
        .digest(canonical.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

/**
 * Decides what has to happen to make the phone and the server agree, for one
 * table, without touching either.
 *
 * It is a three-way merge: local row, remote row, and the link recording what
 * they both looked like last time. Comparing all three is what distinguishes
 * "the server has something new for me" from "I deleted this and the server
 * has not caught up".
 *
 * When both sides changed the same row in different ways, **the phone wins**.
 * That is a deliberate simplification: the user is holding the device they
 * just edited, so silently replacing their work with another device's is the
 * worse surprise.
 *
 * Pure and total — no IO, no clock, no randomness beyond [newRemoteId] — so
 * the merge can be unit-tested exhaustively.
 */
fun planSync(
    local: List<LocalItem>,
    remote: List<RemoteItem>,
    links: List<SyncLink>,
    newRemoteId: (LocalItem) -> String,
): EntityPlan {
    val localByKey = local.associateBy { it.localKey }
    val remoteById = remote.associateBy { it.remoteId }

    val push = linkedMapOf<String, Map<String, String?>>()
    val softDelete = mutableListOf<String>()
    val insertLocal = mutableListOf<RemoteItem>()
    val updateLocal = linkedMapOf<String, RemoteItem>()
    val deleteLocal = mutableListOf<String>()
    val newLinks = mutableListOf<SyncLink>()

    // A duplicate link would otherwise let one row be planned twice.
    val linkedLocal = mutableSetOf<String>()
    val linkedRemote = mutableSetOf<String>()

    fun pushLocal(item: LocalItem, remoteId: String) {
        push[remoteId] = item.fields
        newLinks += SyncLink(item.localKey, remoteId, hashOf(item.fields))
    }

    // ── rows both sides already know about ────────────────────────────────
    links.forEach { link ->
        if (link.localKey in linkedLocal || link.remoteId in linkedRemote) return@forEach
        linkedLocal += link.localKey
        linkedRemote += link.remoteId

        val localItem = localByKey[link.localKey]
        val remoteItem = remoteById[link.remoteId]
        val localChanged = localItem != null && hashOf(localItem.fields) != link.hash

        when {
            // Gone from both. Drop the link with it.
            localItem == null && remoteItem == null -> Unit

            // Deleted here, still live there: tell the server.
            localItem == null -> if (!remoteItem!!.deleted) softDelete += remoteItem.remoteId

            // Deleted there. If the user has since edited it locally that edit
            // is newer than the delete, so it comes back; otherwise it goes.
            remoteItem == null || remoteItem.deleted ->
                if (localChanged) pushLocal(localItem, link.remoteId)
                else deleteLocal += localItem.localKey

            else -> {
                val remoteHash = hashOf(remoteItem.fields)
                val remoteChanged = remoteHash != link.hash
                when {
                    // Untouched on both sides.
                    !localChanged && !remoteChanged -> newLinks += link

                    // Both edited, but to the same value — nothing to move,
                    // just agree on the new hash.
                    remoteHash == hashOf(localItem.fields) ->
                        newLinks += link.copy(hash = remoteHash)

                    // Local edit, or a genuine conflict: the phone wins.
                    localChanged -> pushLocal(localItem, link.remoteId)

                    // Only the server changed: take theirs.
                    else -> {
                        updateLocal[localItem.localKey] = remoteItem
                        newLinks += link.copy(hash = remoteHash)
                    }
                }
            }
        }
    }

    // ── rows only the phone has ───────────────────────────────────────────
    local.filter { it.localKey !in linkedLocal }.forEach { item ->
        val remoteId = newRemoteId(item)
        linkedLocal += item.localKey
        linkedRemote += remoteId
        pushLocal(item, remoteId)
    }

    // ── rows only the server has ──────────────────────────────────────────
    // Tombstones are skipped: a row that is deleted there and absent here is
    // already in the state both sides want.
    remote.filter { it.remoteId !in linkedRemote && !it.deleted }
        .forEach { insertLocal += it }

    return EntityPlan(push, softDelete, insertLocal, updateLocal, deleteLocal, newLinks)
}
