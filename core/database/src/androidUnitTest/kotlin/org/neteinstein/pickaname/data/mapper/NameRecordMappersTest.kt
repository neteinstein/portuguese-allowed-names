package org.neteinstein.pickaname.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.neteinstein.pickaname.data.local.database.NameRecord
import org.neteinstein.pickaname.domain.model.Gender

class NameRecordMappersTest {

    @Test
    fun `toFilterInitial strips diacritics and upper-cases`() {
        assertThat("Áddison".toFilterInitial()).isEqualTo("A")
        assertThat("ãdi".toFilterInitial()).isEqualTo("A")
        assertThat("Zoé".toFilterInitial()).isEqualTo("Z")
        assertThat("kevin".toFilterInitial()).isEqualTo("K")
    }

    @Test
    fun `toFilterInitial returns empty string for empty input`() {
        assertThat("".toFilterInitial()).isEmpty()
    }

    @Test
    fun `gender code round trips through entity and domain mapping`() {
        assertThat(Gender.FEMALE.toEntityCode()).isEqualTo("F")
        assertThat(Gender.MALE.toEntityCode()).isEqualTo("M")
        assertThat("F".toDomainGender()).isEqualTo(Gender.FEMALE)
        assertThat("M".toDomainGender()).isEqualTo(Gender.MALE)
    }

    @Test
    fun `toDomainGender throws on unknown code`() {
        runCatching { "X".toDomainGender() }
            .onSuccess { throw AssertionError("expected failure for unknown gender code") }
    }

    @Test
    fun `NameRecord toDomain maps fields`() {
        val record = NameRecord(id = 7, name = "Alex", gender = "F", initialLetter = "A")

        val domain = record.toDomain()

        assertThat(domain.id).isEqualTo(7)
        assertThat(domain.name).isEqualTo("Alex")
        assertThat(domain.gender).isEqualTo(Gender.FEMALE)
    }
}
