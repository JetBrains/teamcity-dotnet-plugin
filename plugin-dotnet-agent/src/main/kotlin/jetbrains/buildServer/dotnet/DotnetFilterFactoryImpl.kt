package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitTests.SplitTestsFilterSettings

class DotnetFilterFactoryImpl(
    private val _testsFilterProvider: TestsFilterProvider,
    private val _splitTestsFilterSettings: SplitTestsFilterSettings,
    private val _testRunSettingsFileProvider: TestRunSettingsFileProvider
) : DotnetFilterFactory {
    override fun createFilter(command: DotnetCommandType): DotnetFilter {
        var filterExpression = _testsFilterProvider.filterExpression
        val isSplitting = _splitTestsFilterSettings.isActive
        if (isSplitting && filterExpression.length > MaxArgSize) {
            val settingsFile = _testRunSettingsFileProvider.tryGet(command)
            if (settingsFile != null) {
                return DotnetFilter("", settingsFile, true)
            }
        }

        return DotnetFilter(filterExpression, null, isSplitting)
    }

    companion object {
        internal const val MaxArgSize = 2048
    }
}