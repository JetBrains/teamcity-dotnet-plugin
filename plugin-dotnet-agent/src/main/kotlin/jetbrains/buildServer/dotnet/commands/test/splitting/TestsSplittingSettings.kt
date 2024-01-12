

package jetbrains.buildServer.dotnet.commands.test.splitting

interface TestsSplittingSettings {
    val filterType: TestsSplittingFilterType
    val testClasses: Sequence<String>
    val hasEnoughTestClassesToActivateSuppression: Boolean
    val exactMatchFilterSize: Int
    val testsClassesFilePath: String?
    val testClassParametersProcessingMode: TestClassParametersProcessingMode
}