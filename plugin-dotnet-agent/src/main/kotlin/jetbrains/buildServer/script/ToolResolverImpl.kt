package jetbrains.buildServer.script

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class ToolResolverImpl(
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService,
        private val _versionResolver: AnyVersionResolver)
    : ToolResolver {
    override fun resolve(): File {
        var toolPath = _parametersService
                .tryGetParameter(ParameterType.Runner, ScriptConstants.CLT_PATH)
                ?.let { File(it, "tools") }
                ?: throw RunBuildException("C# script runner path was not defined.")

        if(!_fileSystemService.isExists(toolPath)) {
            throw RunBuildException("$toolPath was not found.")
        }

        if(_fileSystemService.isDirectory(toolPath)) {
            val framework = _parametersService
                    .tryGetParameter(ParameterType.Runner, ScriptConstants.FRAMEWORK)
                    ?.let { Framework.tryParse(it) }
                    ?: Framework.Any

            LOG.debug("Framework: ${framework.tfm}")
            val basePath = if(framework == Framework.Any) _versionResolver.resolve(toolPath) else File(toolPath, framework.tfm)
            LOG.debug("Base path: $basePath")
            toolPath = File(File(basePath, "any"), ToolExecutable)
        }

        if(!_fileSystemService.isFile(toolPath)) {
            throw RunBuildException("Cannot find $toolPath.")
        }

        return toolPath
    }

    companion object {
        private val LOG = Logger.getLogger(ToolResolverImpl::class.java)
        const val ToolExecutable = "dotnet-csi.dll"
    }
}