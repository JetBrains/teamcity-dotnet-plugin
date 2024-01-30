package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.util.MultiMap
import java.io.File

class DotnetCoverageProcessorReportPublisherImpl(
    private val _uploader: ArtifactsUploader,
    private val _reportProcessors: Array<ReportFilesProcessor>
) : DotnetCoverageProcessorReportPublisher {

    private val _coverageTypeToProcessors: MultiMap<String, ReportFilesProcessor> = MultiMap<String, ReportFilesProcessor>()

    override fun publishReport(build: DotnetCoverageParameters,
                               type: String,
                               result: DotnetCoverageGenerationResult) {

        if (!result.publishReportFiles()) return

        for (processor in getReportProcessors(type)) {
            processor.processFiles(build, result)
        }

        _uploader.processFiles(build.getTempDirectory(), build.getConfigurationParameter(CoverageConstants.COVERAGE_PUBLISH_PATH_PARAM), result)
    }

    @Synchronized
    private fun getReportProcessors(coverageType: String): List<ReportFilesProcessor> {
        val list: MutableList<ReportFilesProcessor> = ArrayList()
        list.addAll(_reportProcessors.asList())
        list.addAll(_coverageTypeToProcessors[coverageType])
        return list
    }
}
