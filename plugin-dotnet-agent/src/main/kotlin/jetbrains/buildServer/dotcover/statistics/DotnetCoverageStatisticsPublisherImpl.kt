package jetbrains.buildServer.dotcover.statistics

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.CurrentBuildTracker
import jetbrains.buildServer.messages.serviceMessages.BuildStatisticValue
import jetbrains.buildServer.runner.CoverageConstants
import jetbrains.coverage.report.CoverageStatistics

class DotnetCoverageStatisticsPublisherImpl(
    private val _tracker: CurrentBuildTracker
) : DotnetCoverageStatisticsPublisher {

    override fun publishCoverageStatistics(stats: CoverageStatistics) {
        val build = _tracker.currentBuild
        val logger = build.buildLogger

        stats.lineStats?.let {
            logServiceMessage(logger, CoverageConstants.LINE_COVERED_STATS, it.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.LINE_TOTAL_STATS, it.total.toFloat())
        }

        stats.blockStats?.let {
            logServiceMessage(logger, CoverageConstants.BLOCK_COVERED_STATS, it.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.BLOCK_TOTAL_STATS, it.total.toFloat())
        }

        stats.methodStats?.let {
            logServiceMessage(logger, CoverageConstants.METHOD_COVERED_STATS, it.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.METHOD_TOTAL_STATS, it.total.toFloat())
        }

        stats.classStats?.let {
            logServiceMessage(logger, CoverageConstants.CLASS_COVERED_STATS, it.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.CLASS_TOTAL_STATS, it.total.toFloat())
        }

        stats.statementStats?.let {
            logServiceMessage(logger, CoverageConstants.STATEMENT_COVERED_STATS, it.covered.toFloat())
            logServiceMessage(logger, CoverageConstants.STATEMENT_TOTAL_STATS, it.total.toFloat())
        }
    }

    private fun logServiceMessage(logger: BuildProgressLogger, key: String, value: Float) {
        if (value >= 0) {
            logger.message(BuildStatisticValue(key, value).asString())
        }
    }
}
