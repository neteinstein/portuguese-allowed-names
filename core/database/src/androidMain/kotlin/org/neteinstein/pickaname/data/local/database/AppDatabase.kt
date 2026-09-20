package org.neteinstein.pickaname.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [NameEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nameDao(): NameDao

    companion object {
        const val DATABASE_NAME = "pick_a_name.db"
    }
}
