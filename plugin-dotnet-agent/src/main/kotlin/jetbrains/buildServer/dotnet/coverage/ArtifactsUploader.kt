package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

interface ArtifactsUploader {
    fun processFiles(tempDirectory: File, publishPath: String?, result: DotnetCoverageGenerationResult)
}
