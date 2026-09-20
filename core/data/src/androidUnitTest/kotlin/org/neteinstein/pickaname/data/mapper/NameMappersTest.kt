package org.neteinstein.pickaname.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.neteinstein.pickaname.data.parser.ParsedName
import org.neteinstein.pickaname.domain.model.Gender

class NameMappersTest {

    @Test
    fun `ParsedName toRecord computes gender code and initial letter`() {
        val parsed = ParsedName(name = "Áddison", gender = Gender.FEMALE)

        val entity = parsed.toRecord()

        assertThat(entity.name).isEqualTo("Áddison")
        assertThat(entity.gender).isEqualTo("F")
        assertThat(entity.initialLetter).isEqualTo("A")
    }
}
