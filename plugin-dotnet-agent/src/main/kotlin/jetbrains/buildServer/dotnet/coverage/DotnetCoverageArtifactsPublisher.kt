package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

interface DotnetCoverageArtifactsPublisher {

    fun publishFile(build: DotnetCoverageParameters,
                    file: File,
                    relPath: String)

    fun publishNamedFile(build: DotnetCoverageParameters,
                         fileToPublish: File,
                         relativePath: String,
                         publishedName: String)

    fun publishDirectoryZipped(build: DotnetCoverageParameters,
                               toZip: File,
                               relPath: String,
                               fileName: String)
}
