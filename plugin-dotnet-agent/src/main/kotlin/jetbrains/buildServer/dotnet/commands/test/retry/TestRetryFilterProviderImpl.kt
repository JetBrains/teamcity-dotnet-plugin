package jetbrains.buildServer.dotnet.commands.test.retry

import jetbrains.buildServer.dotnet.commands.test.TestsFilterBuilder
import jetbrains.buildServer.dotnet.commands.test.TestsFilterItem
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
            filterItems = filteredTests.map { createTestFilterItem(it) },
            filterCombineOperator = " | "
        )
    }

    private fun createTestFilterItem(testName: String): TestsFilterItem {
        val argumentsIndex = testName.indexOfAny(charArrayOf('<', '('))
        return when {
            argumentsIndex >= 0 -> TestsFilterItem(
                property = "FullyQualifiedName",
                value = testName.substring(0, argumentsIndex),
                operation = "~"
            )

            else -> TestsFilterItem(
                property = "FullyQualifiedName",
                value = testName,
                operation = "="
            )
        }
    }
}