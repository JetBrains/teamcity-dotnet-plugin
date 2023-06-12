package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

data class DotnetCoverageReportRequest(
    val reportFile: File,
    val snapshot: DotnetCoverageParameters) {

    override fun toString(): String {
        return reportFile.toString()
    }
}
