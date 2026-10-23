package co.edu.uniquindio.entorno.core.util

/** Iniciales (hasta dos) de un nombre, tolerando espacios dobles o vacíos. */
fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")

/** "Marcela Rojas" -> "Marcela R." */
fun shortName(name: String): String {
    val parts = name.split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> ""
        parts.size == 1 -> parts[0]
        else -> "${parts[0]} ${parts[1].first().uppercaseChar()}."
    }
}
