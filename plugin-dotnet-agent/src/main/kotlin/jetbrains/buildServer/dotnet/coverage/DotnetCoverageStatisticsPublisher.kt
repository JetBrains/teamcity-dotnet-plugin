package jetbrains.buildServer.dotnet.coverage

import jetbrains.coverage.report.CoverageStatistics

interface DotnetCoverageStatisticsPublisher {
    fun publishCoverageStatistics(stats: CoverageStatistics)
}
