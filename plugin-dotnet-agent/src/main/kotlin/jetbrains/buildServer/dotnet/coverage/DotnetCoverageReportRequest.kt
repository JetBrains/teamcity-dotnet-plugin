package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
data class DotnetCoverageReportRequest(
    val reportFile: File,
    val snapshot: DotnetCoverageParameters) {

    override fun toString(): String {
        return reportFile.toString()
    }
}
