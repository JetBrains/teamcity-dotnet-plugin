package jetbrains.buildServer.dotcover.report.artifacts

import jetbrains.buildServer.dotcover.report.DotnetCoverageGenerationResult
import java.io.File

interface ArtifactsUploader {
    fun processFiles(tempDirectory: File, publishPath: String?, result: DotnetCoverageGenerationResult)
}
