package jetbrains.buildServer.dotnet.logging

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.ToolType
import java.io.File

class LoggerResolverImpl(
    private val _parametersService: ParametersService,
    private val _fileSystemService: FileSystemService,
    private val _pathsService: PathsService
)
    : LoggerResolver {

    override fun resolve(toolType: ToolType): File =
            loggerHome.let { home ->
                when (toolType) {
                    ToolType.MSBuild -> {
                        getLogger(sequenceOf(
                            DotnetConstants.PARAM_MSBUILD_VERSION,
                            DotnetConstants.PARAM_VSTEST_VERSION
                        ))?.let {
                            return getLoggerAssembly(toolType, home, it.msbuildLogger.path)
                        }
                    }
                    ToolType.VSTest -> {
                        getLogger(sequenceOf(
                            DotnetConstants.PARAM_VSTEST_VERSION,
                            DotnetConstants.PARAM_MSBUILD_VERSION
                        ))?.let {
                            return getLoggerAssembly(toolType, home, it.vstestLogger.path)
                        }
                    }
                    else -> {
                        throw RunBuildException("Unknown tool $toolType")
                    }
                }
            } ?: defaultLoggerHome

    private fun getLoggerAssembly(toolType: ToolType, home: File, path: String): File {
        val loggerAssemblyPath = File(home, path)
        if (!_fileSystemService.isExists(loggerAssemblyPath)) {
            throw RunBuildException("Path \"$loggerAssemblyPath\" to $toolType logger was not found")
        }

        return loggerAssemblyPath
    }

    private fun getLogger(versionParameterNames: Sequence<String>): Logger? {
        val currentTool = versionParameterNames.map { getCurrentTool(it) }.filter { it != null }.firstOrNull() ?: Tool.MSBuildCrossPlatform
        return Logger.values().firstOrNull { it.msbuildTool == currentTool || it.vstestTool == currentTool }
    }

    private val loggerHome: File
        get() {
            val loggerHome = _parametersService.tryGetParameter(
                ParameterType.Runner,
                DotnetConstants.INTEGRATION_PACKAGE_HOME
            )
            if (loggerHome.isNullOrBlank()) {
                return defaultLoggerHome
            }

            val loggerHomePath = File(loggerHome)
            if (!_fileSystemService.isExists(loggerHomePath)) {
                return defaultLoggerHome
            }

            return loggerHomePath
        }

    private val defaultLoggerHome: File
        get() = File(_pathsService.getPath(PathType.Plugin), ToolsDirectoryName)

    private fun getCurrentTool(versionParameterName: String): Tool? {
        _parametersService.tryGetParameter(ParameterType.Runner, versionParameterName)?.let {
            return Tool.tryParse(it)
        }

        return null
    }

    companion object {
        const val ToolsDirectoryName = "tools"
    }
}