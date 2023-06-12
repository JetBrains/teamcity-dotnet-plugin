package jetbrains.buildServer.dotnet.coverage

import java.util.EventListener

interface CoverageEventListener : EventListener {
    fun onReportCreated(result: DotnetCoverageGenerationResult)
}
