package jetbrains.buildServer.dotcover.report

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_PUBLISH_SNAPSHOT_PARAM
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_SNAPSHOT_DCVR
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_SNAPSHOT_FILE_EXTENSION
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageGenerationResult
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageGeneratorInput
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageReportGenerator
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageReportGeneratorRunner
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.coverage.utils.TempFactory
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.VersionComparatorUtil
import jetbrains.coverage.report.CoverageStatistics
import java.io.File
import java.io.IOException

class DotCoverReportGenerator(
    private val _factory: DotCoverParametersFactory,
    private val _htmlReporter: DotCoverTeamCityReportGenerator,
    private val _fetcher: DotCoverVersionFetcher,
    private val _runnerFactory: DotCoverReportRunnerFactory,
    private val _tempFactory: TempFactory
) : DotnetCoverageReportGenerator {

    override fun getCoverageType(): String = CoverageConstants.PARAM_DOTCOVER

    override fun getGeneratorName(): String = "dotCover report generator (recommended)"

    override fun supportCoverage(params: DotnetCoverageParameters) =
        _factory.createDotCoverParameters(params).dotCoverHomePath != null

    override fun parametersEquals(p1: DotnetCoverageParameters, p2: DotnetCoverageParameters): Boolean {
        val f1 = _factory.createDotCoverParameters(p1)
        val f2 = _factory.createDotCoverParameters(p2)
        return DotCoverParameters.equalParameters(f1, f2)
    }

    override fun presentParameters(ps: DotnetCoverageParameters): String {
        return _factory.createDotCoverParameters(ps).present()
    }

    @Throws(IOException::class)
    override fun generateReport(files: Collection<File>, input: DotnetCoverageGeneratorInput): DotCoverCoverageGenerationResult? {
        var currentDotCoverInfo: DotCoverInfo? = null
        val versions = HashSet<String?>()

        for (parameters in input.getParameters()) {
            val generatorRunner = _runnerFactory.getDotCoverReporter(parameters) ?: continue

            val dotCoverInfo = DotCoverInfo(parameters, generatorRunner, _fetcher)
            val hasVersion = dotCoverInfo.hasVersion()
            if (hasVersion) {
                versions.add(dotCoverInfo.versionString)
            }

            if (currentDotCoverInfo == null) {
                currentDotCoverInfo = dotCoverInfo
                continue
            }

            if (!hasVersion) {
                continue
            }

            if (VersionComparatorUtil.compare(dotCoverInfo.versionString, currentDotCoverInfo.versionString) > 0) {
                currentDotCoverInfo = dotCoverInfo
            }
        }

        if (currentDotCoverInfo == null) {
            return null
        }

        if (versions.size > 1) {
            val log: BuildProgressLogger = input.getFirstStepParameters().getBuildLogger()
            val versionsStr = StringUtil.join(", ", versions)
            log.warning("Several versions of dotCover were used (${versionsStr}). " +
                    "dotCover ${currentDotCoverInfo.versionString} was chosen to generate the coverage report.")
        }

        val params: DotnetCoverageParameters = currentDotCoverInfo.parameters
        val reporter = createReporterTool(
            params,
            currentDotCoverInfo.versionString,
            currentDotCoverInfo.myGeneratorRunner
        ) ?: return null

        val merged = merge(files, reporter)
        val xmlReport = reporter.runReportTask(merged)
        val html = File(params.getTempDirectory(), CoverageConstants.COVERAGE_HTML_REPORT_ZIP)

        val stat = _htmlReporter.generateReportHTMLandStats(params.getBuildLogger(), params.getConfigurationParameters(), params.resolvePath("."), xmlReport, html)
        val result = DotCoverCoverageGenerationResult(xmlReport, html, stat, reporter)
        val zipTool = result.zipTool

        zipTool?.let { publishSnapshot(params, merged, result, it) }

        publishDotCoverLogs(params, result)

        reporter.runDeleteTask(listOf(merged))
        return result
    }

    private fun publishDotCoverLogs(params: DotnetCoverageParameters, result: DotCoverCoverageGenerationResult) {
        val logsFolder = resolveLogsFolder(params) ?: return
        result.addFileToPublish(CoverageConstants.DOTCOVER_LOGS, logsFolder)
    }

    private fun resolveLogsFolder(params: DotnetCoverageParameters): File? {
        val logs: String? = params.getConfigurationParameter(CoverageConstants.PARAM_DOTCOVER_LOG_PATH)

        return if (logs == null || StringUtil.isEmptyOrSpaces(logs)) null else params.resolvePath(logs)
    }

    @Throws(IOException::class)
    private fun publishSnapshot(params: DotnetCoverageParameters,
                                merged: File,
                                result: DotCoverCoverageGenerationResult,
                                zipTool: DotCoverReporterZipTool) {

        val check: String? = params.getConfigurationParameter(DOTCOVER_PUBLISH_SNAPSHOT_PARAM)
        if ("false".equals(check, ignoreCase = true)) {
            params.getBuildLogger().warning(
                "Publishing of dotCover snapshot was disabled by '$DOTCOVER_PUBLISH_SNAPSHOT_PARAM' configuration parameter")
            return
        }
        val compressedSnapshotFile = _tempFactory.createTempFile(params.getTempDirectory(), "dotCover",
            DOTCOVER_SNAPSHOT_FILE_EXTENSION, 100)
        zipTool.runZipTask(merged, compressedSnapshotFile)

        if (compressedSnapshotFile.isFile && compressedSnapshotFile.length() > 10) {
            result.addFileToPublish(DOTCOVER_SNAPSHOT_DCVR, compressedSnapshotFile)
        }
    }

    @Throws(IOException::class)
    private fun merge(files: Collection<File>, reporter: DotCoverReporterTool): File {
        //NOTE: even one coverage description file may contain several snapshots, this we have to call merge
        val merge = reporter.runMergeTask(files)
        reporter.runDeleteTask(files)
        return merge
    }

    @Throws(IOException::class)
    override fun getCoverageStatisticsValue(params: DotnetCoverageParameters,
                                            result: DotnetCoverageGenerationResult): CoverageStatistics? {
        val dotResult = result as DotCoverCoverageGenerationResult
        val entry = _htmlReporter.parseStatementCoverage(dotResult.mergedReportFile)
        entry?.let {
            val sg = "DotCover statement coverage was: ${it.covered} of ${it.total} ( ${it.percent}%)"
            LOG.info(sg)
            params.getBuildLogger().message(sg)
        }
        return dotResult.stats
    }

    @Throws(IOException::class)
    private fun createReporterTool(parameters: DotnetCoverageParameters,
                                   dotCoverVersionString: String?,
                                   generatorRunner: DotnetCoverageReportGeneratorRunner): DotCoverReporterTool? {

        val dotCoverVersion = _fetcher.getDotCoverVersion(dotCoverVersionString, parameters)
        val runner = DotCoverToolRunnerImpl(generatorRunner, parameters, _tempFactory)

        parameters.getBuildLogger().message("Use " + dotCoverVersion.displayVersion + " commands set")

        return when (dotCoverVersion) {
            DotCoverVersion.DotCover_1_0 -> DotCover1_0_ReporterImpl(runner, parameters, _tempFactory)

            DotCoverVersion.DotCover_1_1, DotCoverVersion.DotCover_1_2, DotCoverVersion.DotCover_2_0,
            DotCoverVersion.DotCover_2_1, DotCoverVersion.DotCover_2_2, DotCoverVersion.DotCover_2_5,
            DotCoverVersion.DotCover_2_6 -> DotCover1_1_ReporterImpl(
                runner,
                parameters,
                DotCover26CommandsConfigFactory(parameters),
                resolveLogsFolder(parameters),
                _tempFactory
            )

            DotCoverVersion.DotCover_2_7, DotCoverVersion.DotCover_3_0, DotCoverVersion.DotCover_3_1,
            DotCoverVersion.DotCover_3_2, DotCoverVersion.DotCover_10_0 -> DotCover1_1_ReporterImpl(
                runner,
                parameters,
                DotCover27CommandsConfigFactory(parameters),
                resolveLogsFolder(parameters),
                _tempFactory
            )

            DotCoverVersion.DotCover_2016AndHigher -> DotCover1_1_ReporterImpl(
                runner,
                parameters,
                DotCover2016CommandsConfigFactory(parameters),
                resolveLogsFolder(parameters),
                _tempFactory
            )
        }
    }

    private inner class DotCoverInfo(parameters: DotnetCoverageParameters,
                                     generatorRunner: DotnetCoverageReportGeneratorRunner,
                                     fetcher: DotCoverVersionFetcher) {
        val versionString: String?
        private val myParameters: DotnetCoverageParameters
        val myGeneratorRunner: DotnetCoverageReportGeneratorRunner

        init {
            myParameters = parameters
            myGeneratorRunner = generatorRunner
            versionString = fetcher.getDotCoverVersionString(parameters, generatorRunner)
        }

        fun hasVersion(): Boolean {
            return !(versionString == null || "" == versionString)
        }

        val parameters: DotnetCoverageParameters
            get() = myParameters
    }

    companion object {
        private val LOG = Logger.getInstance(DotCoverReportGenerator::class.java.name)
    }
}


