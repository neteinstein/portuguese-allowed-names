package org.neteinstein.pickaname.domain.model

/**
 * Best-effort heuristic for "traditional" Portuguese names. The IRN doesn't publish any such
 * classification, so this approximates it: a name is excluded if it uses the letters K, Y or W
 * (foreign to traditional Portuguese orthography) or has a doubled consonant.
 */
object TraditionalNameRules {

    private val EXCLUDED_LETTERS = setOf('k', 'y', 'w')
    private val CONSONANTS = "bcdfghjlmnpqrstvxz".toSet()

    fun isTraditional(name: String): Boolean {
        val normalized = name.lowercase()
        if (normalized.any { it in EXCLUDED_LETTERS }) return false
        return normalized.zipWithNext().none { (a, b) -> a == b && a in CONSONANTS }
    }
}
