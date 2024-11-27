package jetbrains.buildServer.dotnet.commands.test

object TestsFilterBuilder {
    private const val FilterExpressionChunkSize = 1000;
    private val charactersToEscape = listOf("\\", "\"", "(", ")", "&", "|", "=", "!", "~")

    fun buildFilter(filterItems: List<TestsFilterItem>, filterCombineOperator: String) =
        // https://docs.microsoft.com/en-us/dotnet/core/testing/selective-unit-tests
        filterItems
            .map { it.filterExpression }
            .let { filterElements ->
                when {
                    filterElements.size > FilterExpressionChunkSize -> {
                        // chunks in parentheses '(', ')' are necessary to avoid stack overflow in VSTest filter validator
                        // https://youtrack.jetbrains.com/issue/TW-76381
                        filterElements.chunked(FilterExpressionChunkSize) { chunk -> "(${chunk.joinToString(filterCombineOperator)})" }
                    }
                    else -> filterElements
                }
            }
            .joinToString(filterCombineOperator)

    fun escapeSpecialCharacters(testClassName: String) =
        charactersToEscape.fold(testClassName) { resultName, charToEscape ->
            resultName.replace(charToEscape, "\\$charToEscape")
        }.replace(",", "%2C")
}