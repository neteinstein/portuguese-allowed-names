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
 * 2. A spelling heuristic for everything else: a name is excluded if it uses the letters K, Y or
 *    W, the digraphs "ph"/"th" (Portuguese always spells those sounds "f"/"t"), or a doubled
 *    consonant - except "rr" and "ss", which are native Portuguese digraphs with their own
 *    pronunciation (e.g. "carro", "passo"), not a foreign-spelling artifact like "nn" or "ll".
 */
object TraditionalNameRules {

    private val EXCLUDED_LETTERS = setOf('k', 'y', 'w')
    private val EXCLUDED_DIGRAPHS = listOf("ph", "th")
    private val CONSONANTS = "bcdfghjlmnpqrstvxz".toSet()
    private val NATIVE_DOUBLED_CONSONANTS = setOf('r', 's')

    private val CURATED_TRADITIONAL_NAMES: Set<String> = setOf(
        // Male
        "Abílio", "Adelino", "Adriano", "Afonso", "Agostinho", "Aires", "Alberto", "Albino",
        "Alexandre", "Álvaro", "Amadeu", "Américo", "Anacleto", "André", "Ângelo", "Aníbal",
        "Anselmo", "Antero", "António", "Armando", "Arnaldo", "Artur", "Augusto", "Aurélio",
        "Baltazar", "Bartolomeu", "Belarmino", "Belchior", "Bento", "Bernardino", "Bernardo",
        "Cândido", "Carlos", "Casimiro", "Cipriano", "Constantino", "Cosme", "Custódio", "Daniel",
        "David", "Dinis", "Diogo", "Dionísio", "Domingos", "Duarte", "Edmundo", "Eduardo", "Elias",
        "Emídio", "Ernesto", "Estêvão", "Eugénio", "Eurico", "Fausto", "Feliciano", "Felisberto",
        "Fernando", "Filipe", "Firmino", "Florêncio", "Francisco", "Frederico", "Gabriel",
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
        if (normalized.any { it in EXCLUDED_LETTERS }) return false
        if (EXCLUDED_DIGRAPHS.any { normalized.contains(it) }) return false
        return normalized.zipWithNext().none { (a, b) ->
            a == b && a in CONSONANTS && a !in NATIVE_DOUBLED_CONSONANTS
        }
    }
}
