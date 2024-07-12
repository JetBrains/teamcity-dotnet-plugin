package jetbrains.buildServer.dotcover.report.artifacts

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.IOException

class DotnetCoverageArtifactsPublisherImpl(
    private val _watcher: ArtifactsWatcher,
    private val _fileSystemService: FileSystemService
) : DotnetCoverageArtifactsPublisher {

    /**
     * **NOTE: Filename is not changed here!**
     *
     * @param file    file
     * @param relPath server relative path to publish artifact, i.e. relPath/file.getName()
     */
    override fun publishFile(file: File, relPath: String) {
        if (!file.exists() || !file.isFile) {
            throw RuntimeException("Failed to publish file that does not exists")
        }

        val teamcitySpec = file.absolutePath + "=>" + relPath
        _watcher.addNewArtifactsPath(teamcitySpec)
    }

    override fun publishNamedFile(
        tempDirectory: File,
        fileToPublish: File,
        relativePath: String,
        publishedName: String
    ) {

        if (fileToPublish.name == publishedName) {
            publishFile(fileToPublish, relativePath)
            return
        }

        val artifactDirectory = File(tempDirectory, CoverageConstants.DOTCOVER_ARTIFACTS_DIR)
        try {
            _fileSystemService.createDirectory(artifactDirectory)
        } catch (e: Exception) {
            LOG.warn("Failed to create artifact directory '" + tempDirectory + "'. " + e.message)
            return
        }

        val artifactFile = File(artifactDirectory, publishedName)
        try {
            FileUtil.copy(fileToPublish, artifactFile)
            publishFile(artifactFile, relativePath)
        } catch (e: IOException) {
            LOG.warn("Failed to publish coverage artifact. Failed to copy file " + fileToPublish + " to " + artifactFile + ". " + e.message)
        }
    }

    override fun publishDirectoryZipped(toZip: File, relPath: String, fileName: String) {
        require(fileName.endsWith(".zip")) { "fileName must have .zip extension" }

        _watcher.addNewArtifactsPath(toZip.absolutePath + " => " + relPath + "/" + fileName)
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetCoverageArtifactsPublisherImpl::class.java.name)
    }
}
