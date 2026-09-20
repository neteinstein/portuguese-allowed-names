package org.neteinstein.pickaname.data.local.database

import kotlinx.browser.localStorage
import org.w3c.dom.Storage
import org.w3c.dom.get
import org.w3c.dom.set

/**
 * The web names store: [SnapshotNameLocalDataSource] over browser `localStorage`.
 *
 * `localStorage` rather than the IndexedDB MIGRATION_PLAN.md's risk R1 originally named - the
 * list fits its ~5 MB budget with room to spare, the API is synchronous (no async plumbing, no
 * schema, no extra dependency), and it is trivially testable in a real browser. If a future
 * source list ever outgrows that budget, writes fail soft and IndexedDB is the upgrade path.
 */
class LocalStorageNameLocalDataSource(
    storage: Storage = localStorage
) : NameLocalDataSource by SnapshotNameLocalDataSource(LocalStorageSnapshotStorage(storage))

private class LocalStorageSnapshotStorage(private val storage: Storage) : NameSnapshotStorage {
    override fun read(): String? = storage[STORAGE_KEY]
    override fun write(value: String) {
        storage[STORAGE_KEY] = value
    }
}

private const val STORAGE_KEY = "pick_a_name.names"
