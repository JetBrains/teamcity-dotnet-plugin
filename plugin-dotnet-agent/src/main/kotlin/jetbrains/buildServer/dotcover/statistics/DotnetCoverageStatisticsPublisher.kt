package jetbrains.buildServer.dotcover.statistics

import jetbrains.coverage.report.CoverageStatistics

interface DotnetCoverageStatisticsPublisher {
    fun publishCoverageStatistics(stats: CoverageStatistics)
}
