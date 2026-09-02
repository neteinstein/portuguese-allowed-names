package org.neteinstein.pickaname.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TraditionalNameRulesTest {

    @Test
    fun `a plain name with no excluded letters or doubled consonants is traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Maria")).isTrue()
        assertThat(TraditionalNameRules.isTraditional("Joaquim")).isTrue()
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
    fun `names with a doubled consonant are not traditional`() {
        assertThat(TraditionalNameRules.isTraditional("Anna")).isFalse()
        assertThat(TraditionalNameRules.isTraditional("Emmanuel")).isFalse()
    }

    @Test
    fun `doubled vowels are allowed`() {
        assertThat(TraditionalNameRules.isTraditional("Isaac")).isTrue()
    }
}
