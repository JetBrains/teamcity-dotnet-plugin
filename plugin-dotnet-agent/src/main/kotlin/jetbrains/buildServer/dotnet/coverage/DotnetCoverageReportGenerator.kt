package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotcover.report.DotnetCoverageGenerationResult
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.coverage.report.CoverageStatistics
import java.io.File
import java.io.IOException

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface DotnetCoverageReportGenerator {

    /**
     * @return coverage type of this generator
     */
    fun getCoverageType(): String

    fun getGeneratorName(): String

    /**
     * Equality on coverage parameters
     *
     * @param p1 p1
     * @param p2 p2
     * @return true if coverage parameters are equal
     */
    fun parametersEquals(p1: DotnetCoverageParameters, p2: DotnetCoverageParameters): Boolean

    /**
     * Creates string representation of parameters
     *
     * @param ps parameters to render
     * @return string representation of parameters
     */
    fun presentParameters(ps: DotnetCoverageParameters): String

    /**
     * @param build        current running build
     * @param coverageType coverage type
     * @return true to use this CoverageReportGenerator for the results
     */
    fun supportCoverage(params: DotnetCoverageParameters): Boolean

    /**
     * Generates coverage result and merges coverage report files.
     * If there is no way to generate result and to merge files null should be returned
     *
     * @param build running build
     * @param files coverage rreport files
     * @return coverage generation result
     * @throws IOException on error
     */
    @Throws(IOException::class)
    fun generateReport(files: Collection<File>, input: DotnetCoverageGeneratorInput): DotnetCoverageGenerationResult?

    /**
     * Computes statistics values based on generateReport result recived from the same implementation
     *
     * @param build  current running build
     * @param result result recived from [.generateReport]}
     * @return percentage value or null
     * @throws java.io.IOException on error
     */
    @Throws(IOException::class)
    fun getCoverageStatisticsValue(params: DotnetCoverageParameters, result: DotnetCoverageGenerationResult): CoverageStatistics?
}