package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.runners.FileSystemService
import jetbrains.buildServer.runners.ParameterType
import jetbrains.buildServer.runners.ParametersService
import java.io.File

class DotnetLoggerImpl(
        private val _parametersService: ParametersService,
        private val _fileSystemService: FileSystemService)
    : DotnetLogger {

    override fun tryGetToolPath(logger: Logger): File? {
        val loggerHome =_parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.INTEGRATION_PACKAGE_HOME)
        if(loggerHome.isNullOrBlank()) {
           return null
        }

        val loggerHomePath = File(loggerHome);
        if(!_fileSystemService.isExists(loggerHomePath)) {
            throw RunBuildException("Path \"${loggerHomePath}\" to integration pack was not found")
        }

        val loggerAssemblyPath = File(loggerHomePath, logger.relativePath.path)
        if(!_fileSystemService.isExists(loggerAssemblyPath)) {
            throw RunBuildException("Path \"${loggerAssemblyPath}\" to logger was not found")
        }

        return loggerAssemblyPath;
    }
}