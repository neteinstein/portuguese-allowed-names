package org.neteinstein.pickaname.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguageTest {

    @Test
    fun matches_a_language_tag_regardless_of_region_or_case() {
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromTagOrDefault("pt-PT"))
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromTagOrDefault("pt-BR"))
        assertEquals(AppLanguage.PORTUGUESE, AppLanguage.fromTagOrDefault("PT"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTagOrDefault("en-GB"))
    }

    @Test
    fun falls_back_to_english_for_languages_the_app_has_no_strings_for() {
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTagOrDefault("fr-FR"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTagOrDefault(""))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromTagOrDefault(null))
    }
}
