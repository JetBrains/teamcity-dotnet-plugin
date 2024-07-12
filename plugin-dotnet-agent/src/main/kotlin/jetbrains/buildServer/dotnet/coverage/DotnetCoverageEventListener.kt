package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotcover.report.DotnetCoverageGenerationResult
import java.util.EventListener

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface DotnetCoverageEventListener : EventListener {
    fun onReportCreated(result: DotnetCoverageGenerationResult)
}
