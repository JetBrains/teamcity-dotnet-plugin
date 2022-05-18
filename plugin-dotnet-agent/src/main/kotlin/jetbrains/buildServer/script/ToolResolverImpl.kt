package jetbrains.buildServer.script

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class ToolResolverImpl(
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _versionResolver: ToolVersionResolver)
    : ToolResolver {
    override fun resolve(): CsiTool {
        var toolPath = _parametersService
                .tryGetParameter(ParameterType.Runner, ScriptConstants.CLT_PATH)
                ?.let { File(it, "tools") }
                ?: throw RunBuildException("C# script runner path was not defined.")

        var runtimeVersion = Version.Empty

        if(!_fileSystemService.isExists(toolPath)) {
            throw RunBuildException("$toolPath was not found.")
        }

        if(_fileSystemService.isDirectory(toolPath)) {
            val tool = _versionResolver.resolve(toolPath)
            runtimeVersion = tool.runtimeVersion
            LOG.debug("Base path: ${tool.path}")
            toolPath = File(File(tool.path, "any"), ToolExecutable)
        }

        if(!_fileSystemService.isFile(toolPath)) {
            throw RunBuildException("Cannot find $toolPath.")
        }

        return CsiTool(toolPath, runtimeVersion)
    }

    companion object {
        private val LOG = Logger.getLogger(ToolResolverImpl::class.java)
        const val ToolExecutable = "dotnet-csi.dll"
    }
}