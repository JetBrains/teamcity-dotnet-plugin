package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.OSType
import org.apache.log4j.Logger
import java.io.File

class DotnetToolResolverImpl(
        private val _toolProvider: ToolProvider,
        private val _parametersService: ParametersService,
        private val _virtualContext: VirtualContext)
    : DotnetToolResolver {
    override val paltform: ToolPlatform
        get() = ToolPlatform.CrossPlatform

    override val executable: ToolPath
        get() {
            try {
                var dotnetPath = _parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                LOG.debug("${DotnetConstants.CONFIG_PATH} is \"$dotnetPath\"")
                if (dotnetPath.isNullOrBlank()) {
                    LOG.debug("Try to find ${DotnetConstants.EXECUTABLE} executable.")
                    dotnetPath = _toolProvider.getPath(DotnetConstants.EXECUTABLE)
                    LOG.debug("${DotnetConstants.EXECUTABLE} is \"$dotnetPath\"")
                    if (dotnetPath.isNullOrBlank()) {
                        throw RunBuildException("Cannot find the ${DotnetConstants.EXECUTABLE} executable.")
                    }
                }

                val virtualPath = when {
                    !_virtualContext.isVirtual -> Path(dotnetPath)
                    _virtualContext.targetOSType == OSType.WINDOWS -> Path("dotnet.exe")
                    else -> Path("dotnet")
                }

                return ToolPath(Path(dotnetPath), virtualPath)
            } catch (e: ToolCannotBeFoundException) {
                val exception = RunBuildException(e)
                exception.isLogStacktrace = false
                throw exception
            }
        }

    override val isCommandRequired: Boolean
        get() = true

    companion object {
        private val LOG = Logger.getLogger(DotnetToolResolverImpl::class.java)
    }
}