package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.CurrentBuildTracker
import jetbrains.buildServer.messages.serviceMessages.BuildStatisticValue
import jetbrains.buildServer.runner.CoverageConstants
import jetbrains.coverage.report.CoverageStatistics
import jetbrains.coverage.report.StatEntry

class DotnetCoverageStatisticsPublisherImpl(
    private val _tracker: CurrentBuildTracker) : DotnetCoverageStatisticsPublisher {

    override fun publishCoverageStatistics(stats: CoverageStatistics) {
        val build = _tracker.currentBuild
        val logger = build.buildLogger

        val lineStats: StatEntry? = stats.lineStats
        val blockStats: StatEntry? = stats.blockStats
        val methodStats: StatEntry? = stats.methodStats
        val classStats: StatEntry? = stats.classStats
        val statementStats: StatEntry? = stats.statementStats

        if (statementStats != null) {
            logServiceMessage(logger, CoverageConstants.STATEMENT_COVERED_STATS, statementStats.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.STATEMENT_TOTAL_STATS, statementStats.total.toFloat())
        }

        if (lineStats != null) {
            logServiceMessage(logger, CoverageConstants.LINE_COVERED_STATS, lineStats.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.LINE_TOTAL_STATS, lineStats.total.toFloat())
        }

        if (blockStats != null) {
            logServiceMessage(logger, CoverageConstants.BLOCK_COVERED_STATS, blockStats.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.BLOCK_TOTAL_STATS, blockStats.total.toFloat())
        }

        if (methodStats != null) {
            logServiceMessage(logger, CoverageConstants.METHOD_COVERED_STATS, methodStats.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.METHOD_TOTAL_STATS, methodStats.total.toFloat())
        }

        if (classStats != null) {
            logServiceMessage(logger, CoverageConstants.CLASS_COVERED_STATS, classStats.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.CLASS_TOTAL_STATS, classStats.total.toFloat())
        }
    }

    private fun logServiceMessage(logger: BuildProgressLogger, key: String, value: Float) {
        if (value >= 0) {
            logger.message(BuildStatisticValue(key, value).asString())
        }
    }
}
