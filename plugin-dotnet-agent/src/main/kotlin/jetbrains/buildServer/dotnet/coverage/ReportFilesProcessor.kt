package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters

interface ReportFilesProcessor {
    fun processFiles(build: DotnetCoverageParameters, result: DotnetCoverageGenerationResult)
}
