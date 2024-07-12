package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotcover.report.DotnetCoverageGenerationResult
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface ReportFilesProcessor {
    fun processFiles(build: DotnetCoverageParameters, result: DotnetCoverageGenerationResult)
}
