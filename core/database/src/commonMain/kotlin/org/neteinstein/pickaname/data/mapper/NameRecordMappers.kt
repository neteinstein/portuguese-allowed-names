package org.neteinstein.pickaname.data.mapper

import org.neteinstein.pickaname.data.local.database.NameRecord
import org.neteinstein.pickaname.domain.model.Gender
import org.neteinstein.pickaname.domain.model.NameEntry

private const val GENDER_FEMALE_CODE = "F"
private const val GENDER_MALE_CODE = "M"

fun Gender.toEntityCode(): String = when (this) {
    Gender.FEMALE -> GENDER_FEMALE_CODE
    Gender.MALE -> GENDER_MALE_CODE
}

fun String.toDomainGender(): Gender = when (this) {
    GENDER_FEMALE_CODE -> Gender.FEMALE
    GENDER_MALE_CODE -> Gender.MALE
    else -> error("Unknown persisted gender code: $this")
}

fun NameRecord.toDomain(): NameEntry = NameEntry(id = id, name = name, gender = gender.toDomainGender())

/**
 * Accent/diacritic-stripped, upper-cased first character, so filtering by initial groups
 * "Áddison", "Ãdi" and "Ada" all under "A".
 */
fun String.toFilterInitial(): String {
    val firstChar = firstOrNull() ?: return ""
    val normalized = stripDiacritics(firstChar.toString())
    return normalized.uppercase().ifEmpty { firstChar.toString().uppercase() }
}
