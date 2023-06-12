package jetbrains.buildServer.dotnet.coverage

interface DotnetCoverageReportGeneratorsHolder {
    fun getReportGenerator(type: String): DotnetCoverageReportGenerator?
}
