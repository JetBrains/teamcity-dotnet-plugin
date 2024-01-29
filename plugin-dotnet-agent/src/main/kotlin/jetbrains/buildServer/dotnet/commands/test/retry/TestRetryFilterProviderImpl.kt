package jetbrains.buildServer.dotnet.commands.test.retry

import jetbrains.buildServer.dotnet.commands.test.TestsFilterBuilder
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingMode
import jetbrains.buildServer.rx.Disposable
import jetbrains.buildServer.rx.disposableOf

class TestRetryFilterProviderImpl : TestRetryFilterProvider {
    private var filteredTests: List<String> = emptyList()

    override fun setTestNames(tests: List<String>): Disposable {
        filteredTests = tests
        return disposableOf {
            filteredTests = emptyList()
        }
    }

    override fun getFilterExpression(mode: TestsSplittingMode): String = when {
        filteredTests.isEmpty() -> ""
        else -> TestsFilterBuilder.buildFilter(
            filterProperty = "FullyQualifiedName",
            filterOperation = "=",
            filterValues = filteredTests.map { TestsFilterBuilder.escapeSpecialCharacters(it) },
            filterCombineOperator = " | "
        )
    }
}