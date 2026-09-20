package org.neteinstein.pickaname.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NameDao {

    /**
     * `gender`/`initial` are `NULL` when that filter is inactive; `query` is expected to already
     * be blank-normalized to `NULL` by the caller. SQLite's default `LIKE` case-folds ASCII,
     * which covers the vast majority of searches.
     */
    @Query(
        """
        SELECT * FROM names
        WHERE (:gender IS NULL OR gender = :gender)
          AND (:initial IS NULL OR initialLetter = :initial)
          AND (:query IS NULL OR name LIKE '%' || :query || '%')
        ORDER BY name COLLATE NOCASE ASC, gender ASC
        """
    )
    fun observeNames(gender: String?, initial: String?, query: String?): Flow<List<NameEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM names
        WHERE (:gender IS NULL OR gender = :gender)
          AND (:initial IS NULL OR initialLetter = :initial)
          AND (:query IS NULL OR name LIKE '%' || :query || '%')
        """
    )
    fun observeCount(gender: String?, initial: String?, query: String?): Flow<Int>

    @Query("SELECT COUNT(*) FROM names")
    fun observeTotalCount(): Flow<Int>

    /** Names registered under more than one gender value - used to flag likely-unisex names. */
    @Query(
        """
        SELECT name FROM names
        GROUP BY name
        HAVING COUNT(DISTINCT gender) > 1
        """
    )
    fun observeNamesUsedByBothGenders(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(names: List<NameEntity>)

    @Query("DELETE FROM names")
    suspend fun clearAll()

    /** Purges the whole table and repopulates it with [names] atomically. */
    @Transaction
    suspend fun replaceAll(names: List<NameEntity>) {
        clearAll()
        insertAll(names)
    }
}
