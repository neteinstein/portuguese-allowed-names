package org.neteinstein.pickaname.domain.model

/**
 * Legal gender designation a given name is registered under in the official list.
 * Some names are approved for both, in which case two distinct [org.neteinstein.pickaname.domain.model.NameEntry]
 * rows exist for that name string, one per gender.
 */
enum class Gender {
    FEMALE,
    MALE
}
