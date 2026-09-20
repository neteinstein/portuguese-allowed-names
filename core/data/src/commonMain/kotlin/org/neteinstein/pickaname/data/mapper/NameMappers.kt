package org.neteinstein.pickaname.data.mapper

import org.neteinstein.pickaname.data.local.database.NameRecord
import org.neteinstein.pickaname.data.parser.ParsedName

fun ParsedName.toRecord(): NameRecord = NameRecord(
    name = name,
    gender = gender.toEntityCode(),
    initialLetter = name.toFilterInitial()
)
