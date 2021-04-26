package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.CacheCleaner
import jetbrains.buildServer.agent.runner.CleanType
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class InspectCacheCleaner(
        override val name: String,
        override val type: CleanType,
        private val _runnerType: String,
        private val _pathType: PathType,
        private val _pathService: PathsService,
        private val _fileSystemService: FileSystemService)
    : CacheCleaner {
    override val targets get() = sequenceOf(_pathService.getPath(_pathType, _runnerType))

    override fun clean(target: File) = _fileSystemService.remove(target)
}