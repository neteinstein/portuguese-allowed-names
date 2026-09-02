package org.neteinstein.pickaname.domain.model

/**
 * Combined filter criteria for browsing the names list. All fields are ANDed together.
 * A `null` [gender] or [initial], or a blank [query], means "no restriction on this field".
 */
data class NameFilter(
    val query: String = "",
    val gender: Gender? = null,
    val initial: Char? = null,
    val traditionalOnly: Boolean = false
)
