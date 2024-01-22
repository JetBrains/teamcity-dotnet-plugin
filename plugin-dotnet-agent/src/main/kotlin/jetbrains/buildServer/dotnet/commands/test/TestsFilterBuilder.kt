package jetbrains.buildServer.dotnet.commands.test

object TestsFilterBuilder {
    private const val FilterExpressionChunkSize = 1000;
    private val charactersToEscape = listOf("\\", "\"", "(", ")", "&", "|", "=", "!", "~")

    @Suppress("SameParameterValue")
    fun buildFilter(filterProperties: List<String>, filterOperation: String, filterValues: List<String>, filterCombineOperator: String) =
        // https://docs.microsoft.com/en-us/dotnet/core/testing/selective-unit-tests
        filterProperties.flatMap { filterProperty ->
            filterValues.map { filterValue -> "${filterProperty}${filterOperation}${filterValue}" }
        }.let { filterElements ->
            when {
                filterElements.size > FilterExpressionChunkSize -> {
                    // chunks in parentheses '(', ')' are necessary to avoid stack overflow in VSTest filter validator
                    // https://youtrack.jetbrains.com/issue/TW-76381
                    filterElements.chunked(FilterExpressionChunkSize) { chunk -> "(${chunk.joinToString(filterCombineOperator)})" }
                }
                else -> filterElements
            }
        }.joinToString(filterCombineOperator)

    fun escapeSpecialCharacters(testClassName: String) =
        charactersToEscape.fold(testClassName) { resultName, charToEscape ->
            resultName.replace(charToEscape, "\\$charToEscape")
        }.replace(",", "%2C")
}