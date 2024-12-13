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
        // NUnit includes generic and method parameters in the FullyQualifiedName in case of data-driven (theory) tests
        // Because the test parameters can be randomly generated, we want to retry the entire test
        // Also, .NET 9 doesn't work with angle brackets < > in the vstest filter, even if they are escaped
        // Because of this, we extract the test name without parameters, and match tests using the "Contains" operator instead of "Equals"
        val testParametersIndex = testName.indexOfAny(charArrayOf('<', '('))
        val hasParameters = testParametersIndex >= 0
        val testNameWithoutParameters = if (hasParameters) testName.substring(0, testParametersIndex) else testName

        return TestsFilterItem(
            property = TestsFilterItem.Property.FullyQualifiedName,
            operation = if (hasParameters) TestsFilterItem.Operation.Contains else TestsFilterItem.Operation.Equals,
            value = TestsFilterBuilder.escapeSpecialCharacters(testNameWithoutParameters)
        )
    }
}