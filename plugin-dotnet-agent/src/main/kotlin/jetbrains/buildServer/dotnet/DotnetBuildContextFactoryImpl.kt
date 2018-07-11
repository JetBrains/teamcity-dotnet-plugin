package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import java.io.File

class DotnetBuildContextFactoryImpl(
        private val _fileSystemService: FileSystemService,
        private val _pathService: PathsService,
        private val _dotnetCliToolInfo: DotnetCliToolInfo) : DotnetBuildContextFactory {
    override fun create(command: DotnetCommand): DotnetBuildContext =
            DotnetBuildContext(
                    command,
                    command.targetArguments
                            .flatMap { it.arguments }
                            .map { tryGetSdk(it) }
                            .filterNotNull()
                            .toSet())

    private fun tryGetSdk(targetArgument: CommandLineArgument): DotnetSdk? {
        var targetPath = File(targetArgument.value).parentFile
        if (!_fileSystemService.isExists(targetPath)) {
            targetPath = File(_pathService.getPath(PathType.WorkingDirectory), targetPath.path)
            if (!_fileSystemService.isExists(targetPath)) {
                return null
            }
        }

        val version = _dotnetCliToolInfo.getVersion(targetPath)
        if (version == Version.Empty) {
            return null
        }

        return DotnetSdk(targetArgument, targetPath, version)
    }
}