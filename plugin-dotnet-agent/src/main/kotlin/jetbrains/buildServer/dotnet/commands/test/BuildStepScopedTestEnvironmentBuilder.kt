package jetbrains.buildServer.dotnet.commands.test

import java.nio.file.Path

internal interface BuildStepScopedTestEnvironmentBuilder {
    fun setupEnvironmentForTestReporting()

    fun getTestReportsFilesPathForBuildStep(): Path
}
