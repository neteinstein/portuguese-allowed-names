package org.neteinstein.pickaname.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single approved (name, gender) row from the official names list.
 *
 * [gender] is stored as the raw string "F"/"M" (see `Gender.toEntityCode()` in the mapper package)
 * rather than a Room-converted enum, keeping the entity free of domain-layer types.
 * [initialLetter] is precomputed at insert time (accent-stripped, upper-cased first character of
 * [name]) purely as a fast, indexed filter column.
 */
@Entity(
    tableName = "names",
    indices = [
        Index(value = ["name", "gender"], unique = true),
        Index(value = ["gender"]),
        Index(value = ["initialLetter"])
    ]
)
data class NameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gender: String,
    @ColumnInfo(name = "initialLetter")
    val initialLetter: String
)
