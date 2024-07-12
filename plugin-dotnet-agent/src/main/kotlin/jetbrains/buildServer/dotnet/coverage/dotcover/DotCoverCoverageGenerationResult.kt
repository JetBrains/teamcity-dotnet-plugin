package jetbrains.buildServer.dotnet.coverage.dotcover

import jetbrains.buildServer.dotcover.report.DotnetCoverageGenerationResult
import jetbrains.coverage.report.CoverageStatistics
import java.io.File

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotCoverCoverageGenerationResult(
    private val _mergedReportFile: File,
    reportDir: File,
    private val _stats: CoverageStatistics?,
    private val _tool: DotCoverReporterTool
) : DotnetCoverageGenerationResult(_mergedReportFile, emptyList(), reportDir) {

    init {
        setPublishReportFiles(true)
    }

    val zipTool: DotCoverReporterZipTool?
        get() = if (_tool is DotCoverReporterZipTool) {
            _tool
        } else null

    val mergedReportFile: File get() = _mergedReportFile

    val stats: CoverageStatistics? get() = _stats
}

