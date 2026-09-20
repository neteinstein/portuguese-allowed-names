package org.neteinstein.pickaname.data.parser

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.neteinstein.pickaname.domain.model.Gender

class NameListTextParserTest {

    private val parser = NameListTextParser()

    @Test
    fun `parses standard two column rows`() {
        val text = """
            GÉNERO NOME  GÉNERO NOME
            Femininos Aabirah  Masculinos Aabaj
            Femininos Aaditi  Masculinos Aagambir
        """.trimIndent()

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Aabirah", Gender.FEMALE),
            ParsedName("Aabaj", Gender.MALE),
            ParsedName("Aaditi", Gender.FEMALE),
            ParsedName("Aagambir", Gender.MALE)
        )
    }

    @Test
    fun `skips header, footer, page number and blank lines`() {
        val text = """
            1/88
            
            
            Instituto dos Registos e do Notariado, IP
            Campus de Justiça. Avenida D. João II, 1.08.01, Edifício H. 1990-097 Lisboa
            
            
            GÉNERO NOME  GÉNERO NOME
            Femininos Ada  Masculinos Abel
        """.trimIndent()

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Ada", Gender.FEMALE),
            ParsedName("Abel", Gender.MALE)
        )
    }

    @Test
    fun `parses single column rows on the last page`() {
        val text = """
            GÉNERO NOME    
            Femininos Zoé    
            Femininos Zoei    
        """.trimIndent()

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Zoé", Gender.FEMALE),
            ParsedName("Zoei", Gender.FEMALE)
        )
    }

    @Test
    fun `deduplicates literal duplicate rows from the source document`() {
        val text = """
            Femininos Dianna  Masculinos Edson
            Femininos Dianna  Masculinos Edson
        """.trimIndent()

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Dianna", Gender.FEMALE),
            ParsedName("Edson", Gender.MALE)
        )
    }

    @Test
    fun `keeps a name approved for both genders as two distinct entries`() {
        val text = "Femininos Alex  Masculinos Alex"

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Alex", Gender.FEMALE),
            ParsedName("Alex", Gender.MALE)
        )
    }

    @Test
    fun `stitches a hyphenated name that wraps onto its own line`() {
        // Reproduces the real "Darius-Alexandru" line-wrap artifact found in the source PDF.
        val text = """
            Femininos Celtiane  Masculinos Darius-
            Alexandru
            Femininos Cerasela  Masculinos Darque
        """.trimIndent()

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Celtiane", Gender.FEMALE),
            ParsedName("Darius-Alexandru", Gender.MALE),
            ParsedName("Cerasela", Gender.FEMALE),
            ParsedName("Darque", Gender.MALE)
        )
    }

    @Test
    fun `ignores an orphan continuation line when there is nothing to stitch it to`() {
        val text = """
            Orphan
            Femininos Ada  Masculinos Abel
        """.trimIndent()

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Ada", Gender.FEMALE),
            ParsedName("Abel", Gender.MALE)
        )
    }

    @Test
    fun `preserves exact casing and diacritics of approved spellings`() {
        val text = """
            Femininos Áddison  Masculinos AAron
            Femininos kévim  Masculinos kevin
        """.trimIndent()

        val result = parser.parse(text)

        assertThat(result).containsExactly(
            ParsedName("Áddison", Gender.FEMALE),
            ParsedName("AAron", Gender.MALE),
            ParsedName("kévim", Gender.FEMALE),
            ParsedName("kevin", Gender.MALE)
        )
    }

    @Test
    fun `returns empty list for blank input`() {
        assertThat(parser.parse("")).isEmpty()
        assertThat(parser.parse("   \n   \n")).isEmpty()
    }
}
