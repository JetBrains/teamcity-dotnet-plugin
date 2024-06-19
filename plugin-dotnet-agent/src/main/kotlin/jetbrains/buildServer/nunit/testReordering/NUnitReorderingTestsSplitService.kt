package jetbrains.buildServer.nunit.testReordering

data class TestsSplit(val firstStepTests: List<TestInfo>, val secondStepTests: List<TestInfo>)

class NUnitReorderingTestsSplitService {
    fun splitTests(
        allTests: List<TestInfo>,
        failedTests: List<TestInfo>
    ): TestsSplit {
        val failedTestKeys = failedTests
            .map { it.classNameWithAssembly }
            .toHashSet()

        val firstStepTests = mutableListOf<TestInfo>()
        val secondStepTests = mutableListOf<TestInfo>()

        for (test in allTests) {
            if (failedTestKeys.contains(test.classNameWithAssembly)
                || failedTestKeys.contains(test.className)
            ) {
                firstStepTests.add(test)
            } else {
                secondStepTests.add(test)
            }
        }

        return TestsSplit(firstStepTests, secondStepTests)
    }

    private val TestInfo.classNameWithAssembly: String
        get() = when (assembly) {
            null -> className
            else -> "${assembly.absolutePath}: $className"
        }
}
