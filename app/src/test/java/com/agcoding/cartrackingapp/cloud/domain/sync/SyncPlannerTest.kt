package com.agcoding.cartrackingapp.cloud.domain.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The merge table, as tests.
 *
 * [planSync] is the one piece of the sync where a wrong answer loses or
 * duplicates a user's data rather than just failing loudly, and it is pure —
 * no database, no network, no clock — so every branch is worth pinning down
 * here rather than discovering on a phone.
 */
class SyncPlannerTest {

    private fun local(key: String, name: String) = LocalItem(key, mapOf("name" to name))
    private fun remote(id: String, name: String, deleted: Boolean = false) =
        RemoteItem(id, mapOf("name" to name), deleted)
    private fun link(key: String, id: String, name: String) =
        SyncLink(key, id, hashOf(mapOf("name" to name)))

    private val newId: (LocalItem) -> String = { "new-uuid" }

    private fun plan(
        local: List<LocalItem> = emptyList(),
        remote: List<RemoteItem> = emptyList(),
        links: List<SyncLink> = emptyList(),
    ) = planSync(local, remote, links, newId)

    @Test
    fun `unchanged on both sides does nothing`() {
        val result = plan(listOf(local("1", "A")), listOf(remote("r1", "A")), listOf(link("1", "r1", "A")))
        assertTrue(result.isEmpty)
    }

    @Test
    fun `local edit is pushed`() {
        val result = plan(listOf(local("1", "B")), listOf(remote("r1", "A")), listOf(link("1", "r1", "A")))
        assertEquals(mapOf("r1" to mapOf("name" to "B")), result.push)
    }

    @Test
    fun `remote edit is applied locally`() {
        val result = plan(listOf(local("1", "A")), listOf(remote("r1", "C")), listOf(link("1", "r1", "A")))
        assertEquals(setOf("1"), result.updateLocal.keys)
        assertTrue(result.push.isEmpty())
    }

    @Test
    fun `both edited to the same value only refreshes the link`() {
        val result = plan(listOf(local("1", "B")), listOf(remote("r1", "B")), listOf(link("1", "r1", "A")))
        assertTrue(result.push.isEmpty())
        assertTrue(result.updateLocal.isEmpty())
        assertEquals(hashOf(mapOf("name" to "B")), result.links.single().hash)
    }

    @Test
    fun `conflicting edits are won by the phone`() {
        val result = plan(listOf(local("1", "B")), listOf(remote("r1", "C")), listOf(link("1", "r1", "A")))
        assertEquals(mapOf("r1" to mapOf("name" to "B")), result.push)
        assertTrue(result.updateLocal.isEmpty())
    }

    @Test
    fun `deleted locally is soft deleted on the server`() {
        val result = plan(remote = listOf(remote("r1", "A")), links = listOf(link("1", "r1", "A")))
        assertEquals(listOf("r1"), result.softDelete)
    }

    @Test
    fun `deleted remotely removes the untouched local row`() {
        val result = plan(
            listOf(local("1", "A")),
            listOf(remote("r1", "A", deleted = true)),
            listOf(link("1", "r1", "A")),
        )
        assertEquals(listOf("1"), result.deleteLocal)
    }

    @Test
    fun `a local edit outlives a remote delete`() {
        val result = plan(
            listOf(local("1", "B")),
            listOf(remote("r1", "A", deleted = true)),
            listOf(link("1", "r1", "A")),
        )
        assertEquals(mapOf("r1" to mapOf("name" to "B")), result.push)
        assertTrue(result.deleteLocal.isEmpty())
    }

    @Test
    fun `a brand new local row gets an id and is pushed`() {
        val result = plan(listOf(local("9", "Z")))
        assertEquals(mapOf("new-uuid" to mapOf("name" to "Z")), result.push)
    }

    @Test
    fun `a brand new remote row is inserted locally`() {
        val result = plan(remote = listOf(remote("r9", "Z")))
        assertEquals(listOf("r9"), result.insertLocal.map { it.remoteId })
    }

    @Test
    fun `gone from both sides does nothing`() {
        val result = plan(remote = listOf(remote("r1", "A", deleted = true)), links = listOf(link("1", "r1", "A")))
        assertTrue(result.isEmpty)
    }

    @Test
    fun `an unlinked tombstone is not resurrected locally`() {
        // The row was deleted on another device before this one ever saw it.
        // Without the deleted check it would look like news and be inserted.
        val result = plan(remote = listOf(remote("r5", "X", deleted = true)))
        assertTrue(result.insertLocal.isEmpty())
    }

    @Test
    fun `a duplicate link is only acted on once`() {
        val duplicate = link("1", "r1", "A")
        val result = plan(
            listOf(local("1", "B")),
            listOf(remote("r1", "A")),
            listOf(duplicate, duplicate),
        )
        assertEquals(1, result.push.size)
        assertEquals(1, result.links.size)
    }

    @Test
    fun `hash ignores key order but not null versus empty`() {
        assertEquals(
            hashOf(mapOf("a" to "1", "b" to "2")),
            hashOf(mapOf("b" to "2", "a" to "1")),
        )
        assertTrue(hashOf(mapOf("a" to null)) != hashOf(mapOf("a" to "")))
    }
}
