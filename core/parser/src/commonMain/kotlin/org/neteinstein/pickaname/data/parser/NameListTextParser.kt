package org.neteinstein.pickaname.data.parser

import org.neteinstein.pickaname.domain.model.Gender

/**
 * Parses the plain text extracted from the official IRN "Lista de Nomes Próprios" PDF.
 *
 * The source document lays names out as a two-column table repeated on every page:
 * ```
 * GÉNERO NOME  GÉNERO NOME
 * Femininos Aabirah  Masculinos Aabaj
 * Femininos Aaditi   Masculinos Aagambir
 * ```
 * i.e. each line holds up to two independent "<gender> <name>" cells, separated by a run of two
 * or more spaces. The two columns are *not* row-aligned (Femininos/Masculinos are just laid out
 * side by side to save paper) and the last page commonly has only one populated cell. This class
 * only depends on plain Kotlin/JVM APIs so it can be exercised by fast unit tests, independent of
 * the Android/pdfbox text-extraction step that produces its input.
 */
class NameListTextParser {

    fun parse(rawText: String): List<ParsedName> {
        val results = mutableListOf<ParsedName>()
        var lastRecordedIndex = -1

        rawText.lineSequence().forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || isNoiseLine(line)) return@forEach

            splitIntoCells(line).forEach { cell ->
                val genderKeyword = cell.substringBefore(' ', missingDelimiterValue = cell)
                val name = cell.substringAfter(' ', missingDelimiterValue = "").trim()
                val gender = genderKeywordToGender(genderKeyword)

                when {
                    gender != null && name.isNotEmpty() -> {
                        results += ParsedName(name = name, gender = gender)
                        lastRecordedIndex = results.lastIndex
                    }
                    // Rare PDF line-wrap artifact: a long hyphenated name overflows onto its own
                    // line with no gender prefix (e.g. "Masculinos Darius-" then "Alexandru" on
                    // the next line). Stitch it back onto the previous entry when it looks safe to.
                    lastRecordedIndex >= 0 && results[lastRecordedIndex].name.endsWith("-") -> {
                        val previous = results[lastRecordedIndex]
                        results[lastRecordedIndex] = previous.copy(name = previous.name + cell.trim())
                    }
                    else -> Unit // Unattributable noise, safely ignored.
                }
            }
        }

        return results.distinct()
    }

    private fun isNoiseLine(line: String): Boolean =
        line.startsWith("GÉNERO", ignoreCase = true) ||
            line.startsWith("Instituto", ignoreCase = true) ||
            line.startsWith("Campus", ignoreCase = true) ||
            PAGE_NUMBER_REGEX.matches(line)

    private fun splitIntoCells(line: String): List<String> =
        line.split(CELL_SEPARATOR_REGEX).map { it.trim() }.filter { it.isNotEmpty() }

    private fun genderKeywordToGender(keyword: String): Gender? = when {
        keyword.equals("Femininos", ignoreCase = true) -> Gender.FEMALE
        keyword.equals("Masculinos", ignoreCase = true) -> Gender.MALE
        else -> null
    }

    private companion object {
        val PAGE_NUMBER_REGEX = Regex("""\d+/\d+""")
        val CELL_SEPARATOR_REGEX = Regex("""\s{2,}""")
    }
}
