package org.neteinstein.pickaname.domain.model

/**
 * Best-effort heuristic for "traditional" Portuguese names. The IRN doesn't publish any such
 * classification, so this approximates it in two layers:
 *
 * 1. [CURATED_TRADITIONAL_NAMES] - well-known classic Portuguese first names (the Catholic
 *    santoral, historical Portuguese royalty/nobility, and names that topped Portuguese
 *    birth-registry rankings for decades). A name here is always traditional. The list isn't
 *    exhaustive - most of the several thousand IRN-approved names aren't on it - so missing from
 *    it falls through to the spelling heuristic below rather than being treated as "not
 *    traditional".
 * 2. A Portuguese orthography/phonotactics heuristic for everything else. A name is excluded if
 *    it:
 *    - uses the letters K, Y or W;
 *    - uses the digraphs "ph"/"th"/"sh"/"tz" (Portuguese spells those sounds "f", "t" and "ch"
 *      natively, and has no "tz" digraph at all);
 *    - has a "q" not immediately followed by "u" (Portuguese always spells /k/ before a front
 *      sound as "qu", e.g. "Joaquim", never a bare "q");
 *    - starts with "s" immediately followed by a consonant (Portuguese phonotactics require a
 *      prosthetic vowel before an "s"-cluster: "Estêvão", not "Stêvão");
 *    - ends in a consonant other than "l", "r", "s", "z", "m" or "n" - the only consonants a
 *      Portuguese word can end in;
 *    - doubles any letter - except the native digraphs "rr" and "ss" (e.g. "carro", "passo"),
 *      which are the only doubled letters Portuguese ever legitimately uses; doubled vowels
 *      ("aa", "ee") get no such exception. Comparison is by exact character, so an accented vowel
 *      is never conflated with its plain counterpart (e.g. "ã" next to "a" isn't a repeat).
 *    - starts with "abd" - the Arabic patronymic/theophoric prefix meaning "servant of" (as in
 *      "Abdel", "Abdul", "Abderrahmane"). These compounds are spelled with letters Portuguese
 *      itself uses, so they pass every phonotactic check above, but the "abd-" name-formation
 *      pattern has no Portuguese equivalent.
 */
object TraditionalNameRules {

    private val EXCLUDED_LETTERS = setOf('k', 'y', 'w')
    private val EXCLUDED_DIGRAPHS = listOf("ph", "th", "sh", "tz")
    private val VOWELS = setOf('a', 'e', 'i', 'o', 'u', 'á', 'à', 'â', 'ã', 'é', 'ê', 'í', 'ó', 'ô', 'õ', 'ú')
    private val CONSONANTS = "bcdfghjlmnpqrstvxz".toSet()
    private val NATIVE_DOUBLED_CONSONANTS = setOf('r', 's')
    private val VALID_FINAL_CONSONANTS = setOf('l', 'r', 's', 'z', 'm', 'n')

    private val CURATED_TRADITIONAL_NAMES: Set<String> = setOf(
        // Male
        "Abílio", "Adelino", "Adriano", "Afonso", "Agostinho", "Aires", "Alberto", "Albino",
        "Alexandre", "Álvaro", "Amadeu", "Américo", "Anacleto", "André", "Ângelo", "Aníbal",
        "Anselmo", "Antero", "António", "Armando", "Arnaldo", "Artur", "Augusto", "Aurélio",
        "Baltazar", "Bartolomeu", "Belarmino", "Belchior", "Bento", "Bernardino", "Bernardo",
        "Cândido", "Carlos", "Casimiro", "Cipriano", "Constantino", "Cosme", "Custódio", "Daniel",
        "David", "Dinis", "Diogo", "Dionísio", "Domingos", "Duarte", "Edmundo", "Eduardo", "Elias",
        "Emídio", "Ernesto", "Estêvão", "Eugénio", "Eurico", "Fausto", "Feliciano", "Felisberto",
        "Félix", "Fernando", "Filipe", "Firmino", "Florêncio", "Francisco", "Frederico", "Gabriel",
        "Gaspar", "Gastão", "Gerardo", "Germano", "Gil", "Gilberto", "Gonçalo", "Graciano",
        "Gregório", "Guilherme", "Gustavo", "Henrique", "Hermínio", "Hilário", "Horácio", "Hugo",
        "Humberto", "Inácio", "Isaías", "Isidro", "Ivo", "Jacinto", "Jaime", "Januário",
        "Jerónimo", "Joaquim", "João", "Joel", "Jorge", "José", "Júlio", "Justino", "Ladislau",
        "Leandro", "Leonardo", "Leonel", "Lino", "Lopo", "Lourenço", "Lucas", "Luciano", "Luís",
        "Manuel", "Marcelino", "Marcelo", "Marcos", "Mário", "Martim", "Martinho", "Mateus",
        "Máximo", "Miguel", "Modesto", "Narciso", "Nicolau", "Norberto", "Nuno", "Octávio",
        "Olegário", "Onofre", "Osvaldo", "Pantaleão", "Paulino", "Paulo", "Pedro", "Plácido",
        "Policarpo", "Rafael", "Raimundo", "Ramiro", "Raul", "Remígio", "Ricardo", "Roberto",
        "Rodolfo", "Rodrigo", "Rogério", "Romão", "Romeu", "Rosendo", "Rui", "Sabino", "Salvador",
        "Sancho", "Santiago", "Saturnino", "Sebastião", "Sérgio", "Severino", "Silvestre",
        "Silvino", "Simão", "Teodoro", "Teotónio", "Tiago", "Tibério", "Tomás", "Ulisses",
        "Urbano", "Valdemar", "Valentim", "Valério", "Vasco", "Ventura", "Vicente", "Vítor",
        "Xavier", "Zacarias",
        // Female
        "Adelaide", "Adelina", "Adosinda", "Águeda", "Aldina", "Alexandra", "Alice", "Amália",
        "Amélia", "Ana", "Anabela", "Angelina", "Antónia", "Aurora", "Beatriz", "Benedita",
        "Berta", "Branca", "Camila", "Carlota", "Carmo", "Catarina", "Cecília", "Celeste", "Clara",
        "Conceição", "Constança", "Cristina", "Deolinda", "Diamantina", "Dionísia", "Dulce",
        "Edite", "Elisa", "Elisabete", "Elvira", "Emília", "Ermelinda", "Ernestina", "Esperança",
        "Etelvina", "Eugénia", "Eulália", "Eunice", "Fátima", "Felismina", "Fernanda", "Filipa",
        "Filomena", "Firmina", "Flora", "Florbela", "Florinda", "Francisca", "Gabriela",
        "Genoveva", "Georgina", "Gertrudes", "Glória", "Graça", "Gracinda", "Guilhermina",
        "Helena", "Hermínia", "Idalina", "Ilda", "Inês", "Iolanda", "Irene", "Isabel", "Isaura",
        "Isolina", "Ivone", "Joana", "Joaquina", "Judite", "Júlia", "Juliana", "Justina", "Laura",
        "Laurinda", "Leonor", "Leopoldina", "Lídia", "Lígia", "Lurdes", "Lúcia", "Luciana",
        "Ludovina", "Luísa", "Madalena", "Mafalda", "Manuela", "Marcelina", "Margarida", "Maria",
        "Marília", "Marina", "Marta", "Matilde", "Mercedes", "Micaela", "Nazaré", "Noémia",
        "Odete", "Olímpia", "Ondina", "Palmira", "Paula", "Paulina", "Perpétua", "Piedade",
        "Purificação", "Regina", "Remédios", "Rita", "Rosa", "Rosalina", "Rosário", "Salomé",
        "Sandra", "Saudade", "Sebastiana", "Serafina", "Silvina", "Sofia", "Susana", "Teodora",
        "Teresa", "Umbelina", "Urraca", "Valentina", "Vera", "Vitória", "Zulmira"
    ).map { it.lowercase() }.toSet()

    fun isTraditional(name: String): Boolean {
        val normalized = name.lowercase()
        if (normalized in CURATED_TRADITIONAL_NAMES) return true
        if (normalized.startsWith("abd")) return false
        if (normalized.any { it in EXCLUDED_LETTERS }) return false
        if (EXCLUDED_DIGRAPHS.any { normalized.contains(it) }) return false
        if (normalized.indices.any { i -> normalized[i] == 'q' && normalized.getOrNull(i + 1) != 'u' }) return false
        if (normalized.length >= 2 && normalized[0] == 's' && normalized[1] !in VOWELS) return false
        val lastChar = normalized.lastOrNull()
        if (lastChar != null && lastChar in CONSONANTS && lastChar !in VALID_FINAL_CONSONANTS) return false
        return normalized.zipWithNext().none { (a, b) ->
            a == b && (a in VOWELS || (a in CONSONANTS && a !in NATIVE_DOUBLED_CONSONANTS))
        }
    }
}
