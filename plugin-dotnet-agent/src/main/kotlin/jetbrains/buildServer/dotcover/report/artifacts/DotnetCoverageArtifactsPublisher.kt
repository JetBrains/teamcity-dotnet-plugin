package jetbrains.buildServer.dotcover.report.artifacts

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
