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
            when(toolType) {
                ToolType.MSBuild -> {
                    getLogger(sequenceOf(DotnetConstants.PARAM_MSBUILD_VERSION, DotnetConstants.PARAM_VSTEST_VERSION))?.let {
                        return getLoggerAssembly(toolType, home, it.msbuildLogger.path)
                    }
                }
                ToolType.VSTest -> {
                    getLogger(sequenceOf(DotnetConstants.PARAM_VSTEST_VERSION, DotnetConstants.PARAM_MSBUILD_VERSION))?.let {
                        return getLoggerAssembly(toolType, home, it.vstestLogger.path)
                    }
                }
                else -> {
                    throw RunBuildException("Unknown tool ${toolType}")
                }
            }
        }

        return null
    }

    private fun getLoggerAssembly(toolType: ToolType, home:File, path:String): File {
        val loggerAssemblyPath = File(home, path)
        if(!_fileSystemService.isExists(loggerAssemblyPath)) {
            throw RunBuildException("Path \"${loggerAssemblyPath}\" to ${toolType} logger was not found")
        }

        return loggerAssemblyPath
    }

    private fun getLogger(versionParameterNames: Sequence<String>): Logger? {
        val currentTool = versionParameterNames.map { getCurrentTool(it) }.filter { it != null }.firstOrNull() ?: Tool.MSBuild15CrossPlatform
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

    private fun getCurrentTool(versionParameterName: String): Tool? {
        _parametersService.tryGetParameter(ParameterType.Runner, versionParameterName)?.let {
            return Tool.tryParse(it)
        }

        return null
    }
}