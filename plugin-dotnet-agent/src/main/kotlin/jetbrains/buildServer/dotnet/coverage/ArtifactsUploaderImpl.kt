package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_HTML_REPORT_ZIP
import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_REPORT_EXT
import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_REPORT_HOME
import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_REPORT_MULTIPLE
import jetbrains.buildServer.dotnet.CoverageConstants.COVERAGE_REPORT_NAME
import jetbrains.buildServer.util.StringUtil
import java.io.File

class ArtifactsUploaderImpl(private val _artifactsPublisher: DotnetCoverageArtifactsPublisher) : ArtifactsUploader {

    override fun processFiles(tempDirectory: File,
                              publishPath: String?,
                              result: DotnetCoverageGenerationResult) {
        publishReportDataFiles(tempDirectory, result)
        publishExtraFiles(tempDirectory, publishPath, result)
        publishHtmlReport(tempDirectory, result)
    }

    private fun publishHtmlReport(tempDirectory: File,
                                  result: DotnetCoverageGenerationResult) {
        val report: File? = result.htmlReport
        report?.let {
            if (it.isDirectory) {
                _artifactsPublisher.publishDirectoryZipped(
                    it,
                    COVERAGE_REPORT_HOME,
                    COVERAGE_HTML_REPORT_ZIP
                )
            }
            if (it.isFile) {
                _artifactsPublisher.publishNamedFile(tempDirectory, it, COVERAGE_REPORT_HOME, COVERAGE_HTML_REPORT_ZIP)
            }
        }
    }

    private fun publishExtraFiles(tempDirectory: File,
                                  publishPath: String?,
                                  result: DotnetCoverageGenerationResult) {

        val filesToPublish: Set<Map.Entry<String, File>> = result.getFilesToPublish().entries
        if (filesToPublish.isEmpty()) {
            return
        }

        if (StringUtil.isEmptyOrSpaces(publishPath)) {
            return
        }

        for ((key, toPublish) in filesToPublish) {
            val dest = "$publishPath/$key"
            val sep = dest.lastIndexOf('/')
            val prefix = dest.substring(0, sep)
            val name = dest.substring(sep + 1)
            if (toPublish.isFile) {
                _artifactsPublisher.publishNamedFile(tempDirectory, toPublish, prefix, name)
            }
            if (toPublish.isDirectory) {
                _artifactsPublisher.publishDirectoryZipped(toPublish, prefix, name)
            }
        }
    }

    private fun publishReportDataFiles(tempDirectory: File, result: DotnetCoverageGenerationResult) {
        val resultFile: File? = result.mergedResultFile
        resultFile?.let {
            _artifactsPublisher.publishNamedFile(
                tempDirectory,
                it,
                COVERAGE_REPORT_HOME,
                COVERAGE_REPORT_NAME + COVERAGE_REPORT_EXT
            )
        } ?: run {
            for (file in result.multipleResults) {
                _artifactsPublisher.publishFile(file, COVERAGE_REPORT_MULTIPLE)
            }
        }
    }
}
