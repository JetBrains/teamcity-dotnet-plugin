package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import java.io.File

class DotnetToolResolverImpl(
        private val _toolProvider: ToolProvider,
        private val _parametersService: ParametersService)
    : DotnetToolResolver {
    override val paltform: ToolPlatform
        get() = ToolPlatform.CrossPlatform

    override val executableFile: File
        get() {
            try {
                var dotnetPath = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                LOG.debug("${DotnetConstants.CONFIG_PATH} is \"$dotnetPath\"")
                if (dotnetPath.isNullOrBlank()) {
                    // Detect .NET CLI path in place (in the case of docker wrapping).
                    LOG.debug("Try to find ${DotnetConstants.EXECUTABLE} executable.")
                    dotnetPath = _toolProvider.getPath(DotnetConstants.EXECUTABLE)
                    LOG.debug("${DotnetConstants.EXECUTABLE} is \"$dotnetPath\"")
                    if (dotnetPath.isNullOrBlank()) {
                        throw RunBuildException("Cannot find the ${DotnetConstants.EXECUTABLE} executable.")
                    }
                }
                return File(dotnetPath)
            } catch (e: ToolCannotBeFoundException) {
                val exception = RunBuildException(e)
                exception.isLogStacktrace = false
                throw exception
            }
        }

    override val isCommandRequired: Boolean
        get() = true

    companion object {
        private val LOG = Logger.getInstance(DotnetToolResolverImpl::class.java.name)
    }
}