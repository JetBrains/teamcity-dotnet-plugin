package jetbrains.buildServer.dotnet.coverage

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParametersHolder
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.MultiMap
import jetbrains.coverage.report.CoverageStatistics
import jetbrains.coverage.report.StatEntry
import java.io.File
import java.text.MessageFormat

class DotnetCoverageProcessor(
    private val _generators: DotnetCoverageReportsMerger,
    private val _reportPublisher: DotnetCoverageProcessorReportPublisher,
    private val _publisher: DotnetCoverageStatisticsPublisher,
    private val _holder: DotnetCoverageParametersHolder) {

    private val _pendingReports: MultiMap<String, DotnetCoverageReportRequest> = MultiMap<String, DotnetCoverageReportRequest>()
    private val _reportEvents: EventDispatcher<CoverageEventListener> = EventDispatcher.create(CoverageEventListener::class.java)

    @Synchronized
    fun cleanupState() {
        _pendingReports.clear()
    }

    private val logger: BuildProgressLogger
        get() = _holder.getCoverageParameters().getBuildLogger()

    @Synchronized
    fun addCoverageReport(coverageType: String, coverageResult: File) {

        if (coverageResult.exists() && coverageResult.isFile && coverageResult.length() > 0) {
            LOG.info(
                "Accepted coverage report file: " + coverageResult.path + " size: " + coverageResult.length() + " type: " + coverageType
            )
            val ps: DotnetCoverageParameters = _holder.getCoverageParameters()
            _pendingReports.putValue(coverageType, DotnetCoverageReportRequest(coverageResult, ps.makeSnapshot()))
        } else {
            val message = MessageFormat.format(
                "Rejected coverage report file: {0} size: {1}. " +
                        "File is empty or does not exist",
                coverageResult.path,
                coverageResult.length()
            )
            LOG.info(message)
            logger.warning(message)
        }
    }

    @Synchronized
    fun processCoverageOnBuildFinish() {
        if (_pendingReports.isEmpty()) return

        val mgs = "Processing " + _pendingReports.size() + " coverage report(s)"
        LOG.info(mgs)
        logger.message(mgs)

        for ((key, value) in _pendingReports.entrySet()) {
            processCoverageTypeOnBuildFinish(key, value)
        }
    }

    private fun generateCoverage(input: DotnetCoverageGeneratorInput): DotnetCoverageGenerationResult? {
        val files: List<File> = input.getFiles()

        val msg = "Generating coverage report by " + input.coverageType + " for files: " + files
        LOG.info(msg)
        input.getLogger().message(msg)

        return try {
            input.generator.generateReport(files, input)
        } catch (e: Exception) {
            val message = "Failed to generate .NET Coverage report for generator '" +
                    input.coverageType + "' " + files + ". " + e.message
            LOG.error(message, e)
            input.getLogger().warning(message)
            null
        }
    }

    private fun processCoverageTypeOnBuildFinish(type: String,
                                                 files: List<DotnetCoverageReportRequest>) {
        val input = _generators.prepareReports(type, files) ?: return
        val result = generateCoverage(input) ?: return
        _reportEvents.multicaster.onReportCreated(result)

        //TODO: Join all coverage processors here!
        publishStats(input.generator, input.getFirstStepParameters(), result)

        //No way to generate multi-coverage-tool report
        _reportPublisher.publishReport(input.getFirstStepParameters(), type, result)
    }

    private fun publishStats(gen: DotnetCoverageReportGenerator,
                             build: DotnetCoverageParameters,
                             result: DotnetCoverageGenerationResult) {

        val value: CoverageStatistics? = try {
            gen.getCoverageStatisticsValue(build, result)
        } catch (e: Exception) {
            val message = "Failed to compute .NET Coverage statistics for " + gen.getGeneratorName() + "' " + e.message
            LOG.error(message, e)
            build.getBuildLogger().warning(message)
            return
        }
        if (value == null) {
            build.getBuildLogger().warning("No statistics values are provided by " + gen.getGeneratorName())
        } else {
            _publisher.publishCoverageStatistics(value)
            val lineStats: StatEntry? = value.lineStats
            val statementStats: StatEntry? = value.statementStats
            val check = """
                The issue could be caused by one of the following:
                - Include / exclude patterns are incorrect
                - Assemblies are compiled without debugging information
                - .pdb files are not available
                - Visual Studio code coverage is enabled for MSTest
                - .testrunconfig is used for MSTest and Visual Studio code coverage is not disabled (CodeCoverage section with enable="true" is present)"""
            if (isEmpty(lineStats) && isEmpty(statementStats)) {
                build.getBuildLogger().warning("No executable code was detected. $check")
            } else if (isNotCovered(lineStats) || isNotCovered(statementStats)) {
                build.getBuildLogger().warning("No covered code was detected. $check")
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            DotnetCoverageProcessor::class.java.name
        )

        private fun isEmpty(entry: StatEntry?): Boolean {
            return entry == null || entry.total <= 0
        }

        private fun isNotCovered(entry: StatEntry?): Boolean {
            return entry != null && entry.covered <= 0 && entry.total > 0
        }
    }
}
