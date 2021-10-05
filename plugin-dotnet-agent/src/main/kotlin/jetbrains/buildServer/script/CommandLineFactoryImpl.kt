package jetbrains.buildServer.script

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.EnvironmentVariables
import java.io.OutputStreamWriter

class CommandLineFactoryImpl(
        private val _pathsService: PathsService,
        private val _toolResolver: ToolResolver,
        private val _nugetEnvironmentVariables: EnvironmentVariables,
        private val _fileSystemService: FileSystemService,
        private val _rspContentFactory: RspContentFactory,
        private val _virtualContext: VirtualContext)
    : CommandLineFactory {

    override fun create(): CommandLine {
        val rspFile = _fileSystemService.generateTempFile(_pathsService.getPath(PathType.AgentTemp), "options", ".rsp")
        _fileSystemService.write(rspFile) {
            OutputStreamWriter(it).use {
                for (line in _rspContentFactory.create()) {
                    it.write(line)
                    it.write("\n")
                }
            }
        }

        val csiTool = _toolResolver.resolve()
        return CommandLine(
                null,
                TargetType.Tool,
                Path(""),
                Path(_pathsService.getPath(PathType.WorkingDirectory).path),
                listOf(
                        CommandLineArgument(_virtualContext.resolvePath(csiTool.path.path)),
                        CommandLineArgument("@${_virtualContext.resolvePath(rspFile.path)}")
                ),
                _nugetEnvironmentVariables.getVariables(csiTool.runtimeVersion).toList()
        )
    }
}