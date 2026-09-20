package org.neteinstein.pickaname.data.parser

import org.neteinstein.pickaname.domain.model.Gender

/** A single parsed (name, gender) row, before it is mapped to a [org.neteinstein.pickaname.data.local.database.NameEntity]. */
data class ParsedName(val name: String, val gender: Gender)
