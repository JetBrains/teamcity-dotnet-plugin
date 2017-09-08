package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.runners.FileSystemService
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import java.io.File

class LoggerResolverImpl(
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService)
    : LoggerResolver {

    override fun resolve(toolType: ToolType): File? {
        loggerHome?.let {
            val home = it;
            logger?.let {
                var path: String
                when(toolType) {
                    ToolType.MSBuild -> {
                        path = it.msbuildLogger.path
                    }
                    ToolType.VSTest -> {
                        path = it.vstestLogger.path
                    }
                    else -> {
                        throw RunBuildException("Unknown tool ${toolType}")
                    }
                }
                val loggerAssemblyPath = File(home, path)
                if(!_fileSystemService.isExists(loggerAssemblyPath)) {
                    throw RunBuildException("Path \"${loggerAssemblyPath}\" to ${toolType} logger was not found")
                }

                return loggerAssemblyPath
            }
        }

        return null
    }

    private val logger: Logger? get() {
        val currentTool = currentTool ?: Tool.MSBuild15CrossPlatform
        return Logger.values().filter { it.msbuildTool == currentTool || it.vstestTool == currentTool }.firstOrNull()
    }

    private val loggerHome: File? get() {
        val loggerHome =_parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.INTEGRATION_PACKAGE_HOME)
        if(loggerHome.isNullOrBlank()) {
            return null
        }

        val loggerHomePath = File(loggerHome);
        if(!_fileSystemService.isExists(loggerHomePath)) {
            throw RunBuildException("Path \"${loggerHomePath}\" to integration pack was not found")
        }

        return loggerHomePath;
    }

    private val currentTool: Tool? get() {
        _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_MSBUILD_VERSION)?.let {
            return Tool.tryParse(it)
        }

        return null
    }
}