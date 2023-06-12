package jetbrains.buildServer.dotnet.coverage

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.CoverageConstants.DOTCOVER_ARTIFACTS_DIR
import jetbrains.buildServer.dotnet.coverage.utils.FileService
import jetbrains.buildServer.dotnet.coverage.utils.TempFactory
import java.io.File
import java.io.IOException

class DotnetCoverageArtifactsPublisherImpl(
    private val _watcher: ArtifactsWatcher,
    private val _fileService: FileService,
    private val _tempFactory: TempFactory
) : DotnetCoverageArtifactsPublisher {

    /**
     * **NOTE: Filename is not changed here!**
     *
     * @param file    file
     * @param relPath server relative path to publish artifact, i.e. relPath/file.getName()
     */
    override fun publishFile(build: DotnetCoverageParameters,
                             file: File,
                             relPath: String) {
        if (!file.exists() || !file.isFile) {
            throw RuntimeException("Failed to publish file that does not exists")
        }

        val teamcitySpec = file.absolutePath + "=>" + relPath
        _watcher.addNewArtifactsPath(teamcitySpec)
    }

    override fun publishNamedFile(build: DotnetCoverageParameters,
                                  fileToPublish: File,
                                  relativePath: String,
                                  publishedName: String) {

        if (fileToPublish.name == publishedName) {
            publishFile(build, fileToPublish, relativePath)
            return
        }

        val artifactDirectory: File
        val tempDirectory = File(build.getTempDirectory(), DOTCOVER_ARTIFACTS_DIR)
        artifactDirectory = try {
            _tempFactory.createTempDirectory(tempDirectory, 100)
        } catch (e: IOException) {
            logWarning(build, "Failed to create artifact directory '" + tempDirectory + "'. " + e.message)
            return
        }

        val artifactFile = File(artifactDirectory, publishedName)
        try {
            _fileService.copyFile(fileToPublish, artifactFile)
            publishFile(build, artifactFile, relativePath)
        } catch (e: IOException) {
            logWarning(
                build,
                "Failed to publish coverage artifact. Failed to copy file " + fileToPublish + " to " + artifactFile + ". " + e.message
            )
        }
    }

    override fun publishDirectoryZipped(build: DotnetCoverageParameters,
                                        toZip: File,
                                        relPath: String,
                                        fileName: String) {
        require(fileName.endsWith(".zip")) { "fileName must have .zip extension" }

        _watcher.addNewArtifactsPath(toZip.absolutePath + " => " + relPath + "/" + fileName)
    }

    private fun logWarning(build: DotnetCoverageParameters, warn: String) {
        LOG.warn(warn)
        build.getBuildLogger().warning(warn)
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetCoverageArtifactsPublisherImpl::class.java.name)
    }
}
