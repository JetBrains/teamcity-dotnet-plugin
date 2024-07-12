package jetbrains.buildServer.dotnet.coverage

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotnetCoverageReportGeneratorsHolderImpl(
    private val _generators: List<DotnetCoverageReportGenerator>) : DotnetCoverageReportGeneratorsHolder {

    override fun getReportGenerator(type: String): DotnetCoverageReportGenerator? {
        for (generator in _generators) {
            if (generator.getCoverageType().equals(type, ignoreCase = true)) return generator
        }
        return null
    }
}
