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
    fun `doubled vowels are allowed`() {
        assertThat(TraditionalNameRules.isTraditional("Isaac")).isTrue()
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
}
