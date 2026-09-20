package org.neteinstein.pickaname.domain.model

/**
 * A single approved (name, gender) authorization record from the official names list.
 */
data class NameEntry(
    val id: Long,
    val name: String,
    val gender: Gender
)
