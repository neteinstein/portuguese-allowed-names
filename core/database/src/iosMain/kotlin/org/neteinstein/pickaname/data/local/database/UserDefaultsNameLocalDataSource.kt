package org.neteinstein.pickaname.data.local.database

import platform.Foundation.NSUserDefaults

/**
 * The iOS names store: [SnapshotNameLocalDataSource] over `NSUserDefaults`, the same
 * whole-list-at-once shape the web store uses.
 *
 * Room does publish Apple targets, so a SQLite-backed store is possible here in a way it isn't
 * on web - but it would mean a second schema to keep in step for a list this size and shape.
 * Revisit if iOS ever needs per-row writes or queries this can't answer in memory.
 */
class UserDefaultsNameLocalDataSource(
    userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : NameLocalDataSource by SnapshotNameLocalDataSource(UserDefaultsSnapshotStorage(userDefaults))

private class UserDefaultsSnapshotStorage(
    private val userDefaults: NSUserDefaults
) : NameSnapshotStorage {
    override fun read(): String? = userDefaults.stringForKey(STORAGE_KEY)
    override fun write(value: String) {
        userDefaults.setObject(value, STORAGE_KEY)
    }
}

private const val STORAGE_KEY = "pick_a_name.names"
