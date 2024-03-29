package bilboka.messagebot.commands.common

class StringMatchExtractor(
    matchSource: String,
) {
    var matchRemainder: String = matchSource

    fun <T> extract(matchRegex: Regex, extractor: (String) -> T?): T? {
        return matchRegex.findAll(matchRemainder)
            .flatMap { it.groupValues }
            .firstNotNullOfOrNull { value ->
                extractor(value.trim()).also { if (it != null) matchRemainder = getRemainingFrom(value) }
            }
    }

    private fun getRemainingFrom(match: String): String {
        return match.let { matchRemainder.lowercase().split(it.lowercase()).joinToString("").trim() }
    }
}
