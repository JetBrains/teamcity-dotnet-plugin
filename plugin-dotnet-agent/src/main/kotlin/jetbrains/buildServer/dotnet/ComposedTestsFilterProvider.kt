package jetbrains.buildServer.dotnet

class ComposedTestsFilterProvider(
        private val _testsFilterProviders: List<TestsFilterProvider>)
    : TestsFilterProvider {
    override val filterExpression: String
        get() =
            _testsFilterProviders.map { it.filterExpression }.filter { it.isNotBlank() }.let {
                filters ->
                when(filters.size)
                {
                    0 -> ""
                    1 -> "\"${filters[0]}\""
                    else -> "\"${filters.joinToString(" & ") { "($it)" }}\""
                }
            }
}