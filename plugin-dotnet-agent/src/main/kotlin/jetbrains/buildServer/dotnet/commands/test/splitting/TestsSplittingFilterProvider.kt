

package jetbrains.buildServer.dotnet.commands.test.splitting

import jetbrains.buildServer.agent.Logger
import jetbrains.buildServer.dotnet.commands.test.*
import jetbrains.buildServer.dotnet.commands.test.splitting.TestClassParametersProcessingMode.*
import jetbrains.buildServer.dotnet.commands.test.splitting.byTestName.TestsSplittingByNamesReader

class TestsSplittingFilterProvider(
    private val _settings: TestsSplittingSettings,
    private val _testsNamesReader: TestsSplittingByNamesReader,
) : TestsFilterProvider {
    override fun getFilterExpression(mode: TestsSplittingMode): String = when {
        mode.isFilterMode -> {
            val filter = buildFilter(mode)

            LOG.debug("Tests group file filter: \"$filter\".")
            filter
        }
        else -> ""
    }


    private fun buildFilter(mode: TestsSplittingMode) = when {
        mode == TestsSplittingMode.TestNameFilter -> buildExactMatchFilter()
        else -> buildDefaultFilter()
    }

    // FullyQualifiedName~Namespace.TestClass0. | FullyQualifiedName~Namespace.TestClass1. | ...
    // FullyQualifiedName!~Namespace.TestClass0. & FullyQualifiedName!~Namespace.TestClass1. & ...
    private fun buildDefaultFilter(): String {
        val (filterOperation, filterCombineOperator) = when (_settings.filterType) {
            TestsSplittingFilterType.Includes -> Pair(TestsFilterItem.Operation.Contains, " | ")
            TestsSplittingFilterType.Excludes -> Pair(TestsFilterItem.Operation.NotContains, " & ")
        }

        return _settings.testClasses.toList()
            .map { processTestClassParameters(it) }
            .distinct()
            .map { if (testClassContainsParameters(it)) it else "$it." } // to avoid collisions with overlapping test class names prefixes
            .map { TestsFilterItem(property = TestsFilterItem.Property.FullyQualifiedName, operation = filterOperation, value = it) }
            .let { TestsFilterBuilder.buildFilter(filterItems = it, filterCombineOperator = filterCombineOperator) }
    }

    private fun processTestClassParameters(testClass: String): String {
        if (!testClassContainsParameters(testClass)) {
            return testClass
        }
        return when (_settings.testClassParametersProcessingMode) {
            Trim -> testClass.substring(0, testClass.indexOf("("))
            NoProcessing -> testClass
            EscapeSpecialCharacters -> TestsFilterBuilder.escapeSpecialCharacters(testClass)
        }
    }

    private fun testClassContainsParameters(testClass: String) = testClass.contains("(") && testClass.endsWith(")")

    // FullyQualifiedName=Namespace.TestClass0.Test000 | FullyQualifiedName=Namespace.TestClass0.Test001 | ...
    private fun buildExactMatchFilter(): String {
        val (filterOperation, filterCombineOperator) = Pair(TestsFilterItem.Operation.Equals, " | ")

        return _testsNamesReader.read()
            .map { TestsFilterItem(property = TestsFilterItem.Property.FullyQualifiedName, operation = filterOperation, value = it) }
            .toList()
            .let { TestsFilterBuilder.buildFilter(filterItems = it, filterCombineOperator) }
    }

    companion object {
        private val LOG = Logger.getLogger(TestsSplittingFilterProvider::class.java)
    }
}