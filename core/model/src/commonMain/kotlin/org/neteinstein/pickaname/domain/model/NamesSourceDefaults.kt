package org.neteinstein.pickaname.domain.model

/**
 * Default configuration for where the official names list is downloaded from.
 * The user can override this from Settings; [DEFAULT_SOURCE_URL] is what ships out of the box
 * and what "Reset to default" restores.
 */
object NamesSourceDefaults {
    const val DEFAULT_SOURCE_URL: String =
        "https://irn.justica.gov.pt/Portals/33/Regras%20Nome%20Proprio/" +
            "Lista%20Nomes%20Pr%C3%B3prios.pdf?ver=WNDmmwiSO3uacofjmNoxEQ%3D%3D"
}
