

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.retry.TestRetryFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode

class ComposedTestsFilterProvider(
    private val _testRetryFilterProvider: TestRetryFilterProvider,
    private val _testsFilterProviders: List<TestsFilterProvider>
) : TestsFilterProvider {
    override fun getFilterExpression(mode: TestsSplittingMode): String {
        val retryFilter = _testRetryFilterProvider.getFilterExpression(mode)
        if (retryFilter.isNotEmpty()) {
            return retryFilter
        }

        return _testsFilterProviders
            .map { it.getFilterExpression(mode) }
            .filter { it.isNotBlank() }
            .let { filters ->
                when (filters.size) {
                    0 -> ""
                    1 -> filters[0]
                    else -> filters.joinToString(" & ") { "($it)" }
                }
            }
    }
}