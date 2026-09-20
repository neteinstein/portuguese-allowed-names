package org.neteinstein.pickaname.data.local.database

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Android's [NameLocalDataSource]: a thin adapter over the Room [NameDao], translating between
 * the platform-neutral [NameRecord] and Room's [NameEntity]. The SQL in [NameDao] is the
 * reference implementation of this contract - the web store mirrors it by hand.
 */
class RoomNameLocalDataSource(private val nameDao: NameDao) : NameLocalDataSource {

    override fun observeNames(gender: String?, initial: String?, query: String?): Flow<List<NameRecord>> =
        nameDao.observeNames(gender = gender, initial = initial, query = query)
            .map { entities -> entities.map { it.toRecord() } }

    override fun observeCount(gender: String?, initial: String?, query: String?): Flow<Int> =
        nameDao.observeCount(gender = gender, initial = initial, query = query)

    override fun observeTotalCount(): Flow<Int> = nameDao.observeTotalCount()

    override fun observeNamesUsedByBothGenders(): Flow<List<String>> =
        nameDao.observeNamesUsedByBothGenders()

    override suspend fun replaceAll(names: List<NameRecord>) {
        nameDao.replaceAll(names.map { it.toEntity() })
    }
}

internal fun NameEntity.toRecord(): NameRecord =
    NameRecord(id = id, name = name, gender = gender, initialLetter = initialLetter)

// id is left at its default so Room's autoGenerate assigns it, exactly as before this module
// grew a platform-neutral record type.
internal fun NameRecord.toEntity(): NameEntity =
    NameEntity(name = name, gender = gender, initialLetter = initialLetter)
