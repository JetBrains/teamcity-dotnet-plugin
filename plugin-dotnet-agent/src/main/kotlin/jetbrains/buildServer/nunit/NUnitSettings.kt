package jetbrains.buildServer.nunit

import jetbrains.buildServer.nunit.testReordering.TestInfo

interface NUnitSettings {
    val nUnitPath: String?
    val testReorderingEnabled: Boolean
    val testReorderingRecentlyFailedTests: List<TestInfo>
    val appConfigFile: String?
    val additionalCommandLine: String?
    val includeCategories: String?
    val excludeCategories: String?
    val includeTestFiles: String
    val excludeTestFiles: String
    val useProjectFile: Boolean
}
