package jetbrains.buildServer.dotcover.report

import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_REPORT_EXT
import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_REPORT_NAME
import jetbrains.buildServer.dotnet.CoverageConstants.PARAM_DOTCOVER_ARGUMENTS
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.coverage.utils.TempFactory
import jetbrains.buildServer.util.StringUtil
import java.io.File
import java.io.IOException

class DotCover1_1_ReporterImpl(
    private val _runner: DotCoverToolRunner,
    private val _coverageParameters: DotnetCoverageParameters,
    private val _commandsConfigFactory: DotCoverCommandsConfigFactory,
    private val _logsPath: File?,
    private val _tempFactory: TempFactory
) : DotCoverReporterTool, DotCoverReporterZipTool {

    @Throws(IOException::class)
    override fun runDeleteTask(files: Collection<File>) {
        val config = _commandsConfigFactory.createDeleteCommandConfig(files)
        _runner.runDotCoverTool(
            "Remove dotCover snapshot files",
            getDotCoverCommandAdditionalArguments(),
            "delete",
            config
        )
    }

    @Throws(IOException::class)
    override fun runZipTask(snapshotHolderFile: File, destFile: File) {
        val config = _commandsConfigFactory.createZipCommandConfig(snapshotHolderFile, destFile)
        _runner.runDotCoverTool("Packing snapshot files", getDotCoverCommandAdditionalArguments(), "zip", config)
    }

    @Throws(IOException::class)
    override fun runReportTask(reportFile: File): File {
        val resultFile: File = _tempFactory.createTempFile(
            _coverageParameters.getTempDirectory(),
            COVERAGE_REPORT_NAME,
            COVERAGE_REPORT_EXT,
            100
        )
        val config = _commandsConfigFactory.createReportCommandConfig(reportFile, resultFile)
        _runner.runDotCoverTool(
            "Generate dotCover report",
            getDotCoverCommandAdditionalArguments(),
            "report",
            config
        )
        return resultFile
    }

    @Throws(IOException::class)
    override fun runMergeTask(reportFiles: Collection<File>): File {
        val reportFile: File = _tempFactory.createTempFile(
            _coverageParameters.getTempDirectory(),
            "dotCoverSnapshot",
            ".dcvr",
            100)
        val config = _commandsConfigFactory.createMergeCommandConfig(reportFiles, reportFile)
        _runner.runDotCoverTool(
            "Merge dotCover reports",
            getDotCoverCommandAdditionalArguments(),
            "merge",
            config
        )
        return reportFile
    }

    @Throws(IOException::class)
    private fun getDotCoverCommandAdditionalArguments(): Collection<String> {
        val arguments = ArrayList<String>()
        val logFile = getDotCoverLogFile()
        if (logFile != null) {
            arguments.add("/LogFile=$logFile")
        }
        val dotCoverCustomCommandline: String? = _coverageParameters.getRunnerParameter(PARAM_DOTCOVER_ARGUMENTS)
        if (dotCoverCustomCommandline != null) {
            arguments.addAll(listOf(*StringUtil.splitByLines(dotCoverCustomCommandline)))
        }
        return arguments
    }

    @Throws(IOException::class)
    private fun getDotCoverLogFile(): File? {
        return if (_logsPath == null) null
        else _tempFactory.createTempFile(_logsPath, "dotCover", ".log", 100)
    }
}
