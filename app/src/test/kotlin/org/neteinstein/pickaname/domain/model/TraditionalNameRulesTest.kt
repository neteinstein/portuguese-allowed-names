package org.neteinstein.pickaname.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TraditionalNameRulesTest {

    @Test
    fun `a plain name with no excluded letters or doubled consonants is traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Marina")).isTrue()
        assertThat(TraditionalNameRules.isTraditional("Adriano")).isTrue()
    }

    @Test
    fun `names containing k, y or w are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Kevin")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Yara")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Wilson")).isFalse()
    }

    @Test
    fun `the excluded-letter check is case-insensitive`() {
        assertThat(TraditionalNameRules.isTraditional("KEVIN")).isFalse()
    }

    @Test
    fun `names with a doubled consonant other than rr or ss are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Anna")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Emmanuel")).isFalse()
    }

    @Test
    fun `names with a repeated identical vowel are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Isaac")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Aaron")).isFalse()
    }

    @Test
    fun `an accented vowel next to its plain counterpart is not treated as a repeat`() {
        assertThat(TraditionalNameRules.isTraditional("Naãlo")).isTrue()
    }

    @Test
    fun `two identical plain vowels in a row are still rejected regardless of the accented rule`() {
        assertThat(TraditionalNameRules.isTraditional("Naalo")).isFalse()
    }

    @Test
    fun `doubled rr and ss are allowed as native Portuguese digraphs`() {
        assertThat(TraditionalNameRules.isTraditional("Sarra")).isTrue()
        assertThat(TraditionalNameRules.isTraditional("Nissa")).isTrue()
    }

    @Test
    fun `names spelled with ph or th are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Sophia")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Theodoro")).isFalse()
    }

    @Test
    fun `names spelled with sh or tz are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Lasha")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Fritz")).isFalse()
    }

    @Test
    fun `a q not immediately followed by u is not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Qamila")).isFalse()
    }

    @Test
    fun `a q followed by u is traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Aquilino")).isTrue()
    }

    @Test
    fun `a name starting with s followed by a consonant is not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Stavros")).isFalse()
    }

    @Test
    fun `a name starting with s followed by a vowel is traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Samuel")).isTrue()
    }

    @Test
    fun `a name ending in a consonant outside l, r, s, z, m, n is not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Robert")).isFalse()
    }

    @Test
    fun `a name ending in l, r, s, z, m or n is traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Ruben")).isTrue()
    }

    @Test
    fun `the traditional Portuguese spelling of the same names is unaffected`() {
        assertThat(TraditionalNameRules.isTraditional("Sofia")).isTrue()
        assertThat(TraditionalNameRules.isTraditional("Teodoro")).isTrue()
    }

    @Test
    fun `curated classic Portuguese names are traditional regardless of case`() {
        assertThat(TraditionalNameRules.isTraditional("José")).isTrue()
        assertThat(TraditionalNameRules.isTraditional("maria")).isTrue()
        assertThat(TraditionalNameRules.isTraditional("GONÇALO")).isTrue()
    }

    @Test
    fun `names starting with the Arabic abd- prefix are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Abdel")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Abdelhadi")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Abdelrahman")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Abderrahmane")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Abdrahman")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Abducadre")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Abdul")).isFalse()
    }

    @Test
    fun `the abd- prefix check is case-insensitive`() {
        assertThat(TraditionalNameRules.isTraditional("ABDUL")).isFalse()
    }

    @Test
    fun `names with letters outside the Portuguese alphabet are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Björn")).isFalse() // Swedish ö
        assertThat(TraditionalNameRules.isTraditional("Zoë")).isFalse() // French/English diaeresis ë
        assertThat(TraditionalNameRules.isTraditional("Åsa")).isFalse() // Nordic å
        assertThat(TraditionalNameRules.isTraditional("Łukasz")).isFalse() // Polish ł
        assertThat(TraditionalNameRules.isTraditional("Müller")).isFalse() // German ü
    }

    @Test
    fun `names spelled with zh, sch, cz or sz are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Zhu")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Anschel")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Aczar")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Aszter")).isFalse()
    }

    @Test
    fun `a hyphenated compound name spelled entirely in Portuguese is still traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Maria-João")).isTrue()
    }
}
