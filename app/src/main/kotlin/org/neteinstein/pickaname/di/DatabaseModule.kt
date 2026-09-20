package org.neteinstein.pickaname.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.neteinstein.pickaname.data.local.database.AppDatabase
import org.neteinstein.pickaname.data.local.database.NameLocalDataSource
import org.neteinstein.pickaname.data.local.database.RoomNameLocalDataSource

/**
 * Room database + DAO providers, plus the Room-backed [NameLocalDataSource] the (multiplatform)
 * repositories are written against - on web that same interface is served by
 * `LocalStorageNameLocalDataSource` instead, since Room has no wasmJs support.
 */
val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
    single { get<AppDatabase>().nameDao() }
    single<NameLocalDataSource> { RoomNameLocalDataSource(get()) }
}
