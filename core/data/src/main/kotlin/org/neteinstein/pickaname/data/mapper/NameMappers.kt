package org.neteinstein.pickaname.data.mapper

import org.neteinstein.pickaname.data.local.database.NameEntity
import org.neteinstein.pickaname.data.parser.ParsedName

fun ParsedName.toEntity(): NameEntity = NameEntity(
    name = name,
    gender = gender.toEntityCode(),
    initialLetter = name.toFilterInitial()
)
