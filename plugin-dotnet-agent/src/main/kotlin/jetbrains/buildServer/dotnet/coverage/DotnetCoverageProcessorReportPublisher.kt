package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters

interface DotnetCoverageProcessorReportPublisher {

    fun publishReport(build: DotnetCoverageParameters,
                      type: String,
                      result: DotnetCoverageGenerationResult)
}
