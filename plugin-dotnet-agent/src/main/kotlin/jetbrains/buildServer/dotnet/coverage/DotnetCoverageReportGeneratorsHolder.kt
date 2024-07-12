package jetbrains.buildServer.dotnet.coverage

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface DotnetCoverageReportGeneratorsHolder {
    fun getReportGenerator(type: String): DotnetCoverageReportGenerator?
}
