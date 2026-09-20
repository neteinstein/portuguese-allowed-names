package org.neteinstein.pickaname.domain.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TraditionalNameRulesTest {

    @Test
    fun a_plain_name_with_no_excluded_letters_or_doubled_consonants_is_traditional() {
        assertTrue(TraditionalNameRules.isTraditional("Marina"))
        assertTrue(TraditionalNameRules.isTraditional("Adriano"))
    }

    @Test
    fun names_containing_k_y_or_w_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Kevin"))
        assertFalse(TraditionalNameRules.isTraditional("Yara"))
        assertFalse(TraditionalNameRules.isTraditional("Wilson"))
    }

    @Test
    fun the_excluded_letter_check_is_case_insensitive() {
        assertFalse(TraditionalNameRules.isTraditional("KEVIN"))
    }

    @Test
    fun names_with_a_doubled_consonant_other_than_rr_or_ss_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Anna"))
        assertFalse(TraditionalNameRules.isTraditional("Emmanuel"))
    }

    @Test
    fun names_with_a_repeated_identical_vowel_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Isaac"))
        assertFalse(TraditionalNameRules.isTraditional("Aaron"))
    }

    @Test
    fun an_accented_vowel_next_to_its_plain_counterpart_is_not_treated_as_a_repeat() {
        assertTrue(TraditionalNameRules.isTraditional("Naãlo"))
    }

    @Test
    fun two_identical_plain_vowels_in_a_row_are_still_rejected_regardless_of_the_accented_rule() {
        assertFalse(TraditionalNameRules.isTraditional("Naalo"))
    }

    @Test
    fun doubled_rr_and_ss_are_allowed_as_native_Portuguese_digraphs() {
        assertTrue(TraditionalNameRules.isTraditional("Sarra"))
        assertTrue(TraditionalNameRules.isTraditional("Nissa"))
    }

    @Test
    fun names_spelled_with_ph_or_th_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Sophia"))
        assertFalse(TraditionalNameRules.isTraditional("Theodoro"))
    }

    @Test
    fun names_spelled_with_sh_or_tz_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Lasha"))
        assertFalse(TraditionalNameRules.isTraditional("Fritz"))
    }

    @Test
    fun a_q_not_immediately_followed_by_u_is_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Qamila"))
    }

    @Test
    fun a_q_followed_by_u_is_traditional() {
        assertTrue(TraditionalNameRules.isTraditional("Aquilino"))
    }

    @Test
    fun a_name_starting_with_s_followed_by_a_consonant_is_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Stavros"))
    }

    @Test
    fun a_name_starting_with_s_followed_by_a_vowel_is_traditional() {
        assertTrue(TraditionalNameRules.isTraditional("Samuel"))
    }

    @Test
    fun a_name_ending_in_a_consonant_outside_l_r_s_z_m_n_is_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Robert"))
    }

    @Test
    fun a_name_ending_in_l_r_s_z_m_or_n_is_traditional() {
        assertTrue(TraditionalNameRules.isTraditional("Ruben"))
    }

    @Test
    fun the_traditional_Portuguese_spelling_of_the_same_names_is_unaffected() {
        assertTrue(TraditionalNameRules.isTraditional("Sofia"))
        assertTrue(TraditionalNameRules.isTraditional("Teodoro"))
    }

    @Test
    fun curated_classic_Portuguese_names_are_traditional_regardless_of_case() {
        assertTrue(TraditionalNameRules.isTraditional("José"))
        assertTrue(TraditionalNameRules.isTraditional("maria"))
        assertTrue(TraditionalNameRules.isTraditional("GONÇALO"))
    }

    @Test
    fun names_starting_with_the_Arabic_abd__prefix_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Abdel"))
        assertFalse(TraditionalNameRules.isTraditional("Abdelhadi"))
        assertFalse(TraditionalNameRules.isTraditional("Abdelrahman"))
        assertFalse(TraditionalNameRules.isTraditional("Abderrahmane"))
        assertFalse(TraditionalNameRules.isTraditional("Abdrahman"))
        assertFalse(TraditionalNameRules.isTraditional("Abducadre"))
        assertFalse(TraditionalNameRules.isTraditional("Abdul"))
    }

    @Test
    fun the_abd__prefix_check_is_case_insensitive() {
        assertFalse(TraditionalNameRules.isTraditional("ABDUL"))
    }

    @Test
    fun names_with_letters_outside_the_Portuguese_alphabet_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Björn")) // Swedish ö
        assertFalse(TraditionalNameRules.isTraditional("Zoë")) // French/English diaeresis ë
        assertFalse(TraditionalNameRules.isTraditional("Åsa")) // Nordic å
        assertFalse(TraditionalNameRules.isTraditional("Łukasz")) // Polish ł
        assertFalse(TraditionalNameRules.isTraditional("Müller")) // German ü
    }

    @Test
    fun names_spelled_with_zh_sch_cz_or_sz_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Zhu"))
        assertFalse(TraditionalNameRules.isTraditional("Anschel"))
        assertFalse(TraditionalNameRules.isTraditional("Aczar"))
        assertFalse(TraditionalNameRules.isTraditional("Aszter"))
    }

    @Test
    fun a_hyphenated_compound_name_spelled_entirely_in_Portuguese_is_still_traditional() {
        assertTrue(TraditionalNameRules.isTraditional("Maria-João"))
    }

    @Test
    fun names_starting_with_moham_muham_or_abu_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Mohamed"))
        assertFalse(TraditionalNameRules.isTraditional("Mohammad"))
        assertFalse(TraditionalNameRules.isTraditional("Muhammad"))
        assertFalse(TraditionalNameRules.isTraditional("Abubacar"))
    }

    @Test
    fun specific_known_non_Portuguese_given_names_are_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Hassan"))
        assertFalse(TraditionalNameRules.isTraditional("Hussain"))
        assertFalse(TraditionalNameRules.isTraditional("Ibrahim"))
        assertFalse(TraditionalNameRules.isTraditional("Ibrahima"))
        assertFalse(TraditionalNameRules.isTraditional("Mustafa"))
        assertFalse(TraditionalNameRules.isTraditional("Nabil"))
        assertFalse(TraditionalNameRules.isTraditional("Samir"))
        assertFalse(TraditionalNameRules.isTraditional("Nasser"))
        assertFalse(TraditionalNameRules.isTraditional("Sultan"))
        assertFalse(TraditionalNameRules.isTraditional("Bilal"))
        assertFalse(TraditionalNameRules.isTraditional("Rassan"))
        assertFalse(TraditionalNameRules.isTraditional("Omar"))
        assertFalse(TraditionalNameRules.isTraditional("Amir"))
    }

    @Test
    fun the_non_Portuguese_given_name_denylist_is_case_insensitive() {
        assertFalse(TraditionalNameRules.isTraditional("HASSAN"))
    }

    @Test
    fun a_name_used_by_both_genders_is_not_traditional() {
        assertFalse(TraditionalNameRules.isTraditional("Ashley", isUsedByBothGenders = true))
    }

    @Test
    fun a_name_not_used_by_both_genders_is_unaffected_by_the_parameter() {
        assertTrue(TraditionalNameRules.isTraditional("Marina", isUsedByBothGenders = false))
    }

    @Test
    fun a_curated_traditional_name_stays_traditional_even_if_used_by_both_genders() {
        assertTrue(TraditionalNameRules.isTraditional("Carlos", isUsedByBothGenders = true))
        assertTrue(TraditionalNameRules.isTraditional("Joaquim", isUsedByBothGenders = true))
    }
}
