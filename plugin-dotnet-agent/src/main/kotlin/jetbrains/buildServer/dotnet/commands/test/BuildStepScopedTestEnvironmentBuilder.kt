package jetbrains.buildServer.dotnet.commands.test

import jetbrains.buildServer.dotnet.Verbosity
import java.nio.file.Path

internal interface BuildStepScopedTestEnvironmentBuilder {
    fun setupEnvironmentForTestReporting(verbosityLevel: Verbosity?)

    fun getTestReportsFilesPathForBuildStep(): Path
}
