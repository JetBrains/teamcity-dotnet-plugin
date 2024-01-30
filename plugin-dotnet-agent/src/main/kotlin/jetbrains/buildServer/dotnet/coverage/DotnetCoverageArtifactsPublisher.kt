package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

interface DotnetCoverageArtifactsPublisher {

    fun publishFile(file: File, relPath: String)

    fun publishNamedFile(
        tempDirectory: File,
        fileToPublish: File,
        relativePath: String,
        publishedName: String
    )

    fun publishDirectoryZipped(toZip: File, relPath: String, fileName: String)
}
