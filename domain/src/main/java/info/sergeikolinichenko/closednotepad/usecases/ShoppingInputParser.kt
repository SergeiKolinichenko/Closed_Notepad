package info.sergeikolinichenko.closednotepad.usecases

object ShoppingInputParser {

    data class Parsed(val name: String, val quantity: String)

    private const val NUMBER = """\d+(?:[.,]\d+)?"""
    private const val UNIT =
        """(?:шт|кг|г|гр|л|мл|уп|пач|бут|бан|бр|pcs|pc|kg|g|l|ml|pack|x)\.?"""

    private val trailing = Regex("""^(.+?)\s+($NUMBER\s*(?:$UNIT)?)$""", RegexOption.IGNORE_CASE)
    private val leading = Regex("""^($NUMBER\s*(?:$UNIT)?)\s+(.+)$""", RegexOption.IGNORE_CASE)

    // "Яйца 10 шт" -> ("Яйца", "10 шт"), "2 л молока" -> ("Молока", "2 л")
    fun parse(input: String): Parsed {
        val text = input.trim().replace(Regex("""\s+"""), " ")
        trailing.matchEntire(text)?.let { m ->
            return Parsed(m.groupValues[1].capitalizeFirst(), normalize(m.groupValues[2]))
        }
        leading.matchEntire(text)?.let { m ->
            return Parsed(m.groupValues[2].capitalizeFirst(), normalize(m.groupValues[1]))
        }
        return Parsed(text.capitalizeFirst(), "")
    }

    private fun normalize(quantity: String): String =
        Regex("""^($NUMBER)\s*(.*)$""").matchEntire(quantity.trim())
            ?.let { m -> listOf(m.groupValues[1], m.groupValues[2]).filter { it.isNotEmpty() }.joinToString(" ") }
            ?: quantity.trim()

    private fun String.capitalizeFirst(): String =
        if (isEmpty()) this else substring(0, 1).uppercase() + substring(1)
}
