package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher
import jetbrains.buildServer.agent.runner.LoggerService
import java.io.File

class ArtifactServiceImpl(
        private val _fileSystem: FileSystemService,
        private val _artifactsWatcher: ArtifactsWatcher)
    : ArtifactService {

    override fun publish(tool: InspectionTool, artifactSource: File, artifactDestination: Path?): Boolean {
        if (_fileSystem.isExists(artifactSource) && _fileSystem.isFile(artifactSource) && _fileSystem.getLength(artifactSource) > 0L) {
            _artifactsWatcher.addNewArtifactsPath(artifactSource.canonicalPath + "=>" + ".teamcity/${tool.runnerType}/${artifactDestination?.path ?: ""}")
            return true;
        }

        return false
    }
}