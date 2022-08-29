package jetbrains.buildServer.dotnet

class DotnetFilterFactoryImpl(
    private val _testsFilterProvider: TestsFilterProvider,
    private val _splittedTestsFilterSettings: SplittedTestsFilterSettings,
    private val _testRunSettingsFileProvider: TestRunSettingsFileProvider
) : DotnetFilterFactory {
    override fun createFilter(command: DotnetCommandType): DotnetFilter {
        var filterExpression = _testsFilterProvider.filterExpression
        val isSplitting = _splittedTestsFilterSettings.isActive
        if (_splittedTestsFilterSettings.isActive && filterExpression.length > MaxArgSize) {
            val settingsFile = _testRunSettingsFileProvider.tryGet(command)
            if (settingsFile != null) {
                return DotnetFilter("", settingsFile, isSplitting)
            }
        }

        return DotnetFilter(filterExpression, null, isSplitting)
    }

    companion object {
        internal const val MaxArgSize = 2048
    }
}