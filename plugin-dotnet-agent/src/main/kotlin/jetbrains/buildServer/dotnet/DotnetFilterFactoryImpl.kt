

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.commands.test.TestRunSettingsFileProvider
import jetbrains.buildServer.dotnet.commands.test.TestsFilterProvider
import jetbrains.buildServer.dotnet.commands.test.splitting.TestsSplittingModeProvider

class DotnetFilterFactoryImpl(
    private val _testsFilterProvider: TestsFilterProvider,
    private val _testsSplittingModeProvider: TestsSplittingModeProvider,
    private val _testRunSettingsFileProvider: TestRunSettingsFileProvider
) : DotnetFilterFactory {
    override fun createFilter(context: DotnetCommandContext): DotnetFilter {
        val testsSplittingMode = _testsSplittingModeProvider.getMode(context.toolVersion)
        val filterExpression = _testsFilterProvider.getFilterExpression(testsSplittingMode)
        if (filterExpression.length > MaxArgSize) {
            val settingsFile = _testRunSettingsFileProvider.tryGet(context)
            if (settingsFile != null) {
                return DotnetFilter("", settingsFile)
            }
        }

        return DotnetFilter(filterExpression, null)
    }

    companion object {
        internal const val MaxArgSize = 2048
    }
}