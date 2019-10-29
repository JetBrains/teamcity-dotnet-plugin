package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.OSType
import org.apache.log4j.Logger
import java.io.File

class DotnetToolResolverImpl(
        private val _parametersService: ParametersService,
        private val _toolEnvironment: ToolEnvironment,
        private val _toolSearchService: ToolSearchService,
        private val _environment: Environment,
        private val _virtualContext: VirtualContext)
    : DotnetToolResolver {
    override val paltform: ToolPlatform
        get() = ToolPlatform.CrossPlatform

    override val executable: ToolPath
        get() {
            try {
                val executables = _toolSearchService.find(DotnetConstants.EXECUTABLE, _toolEnvironment.homePaths).map { Path(it.path) } + tryFinding(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                val dotnetPath = executables.firstOrNull() ?: throw RunBuildException("Cannot find the ${DotnetConstants.EXECUTABLE} executable.")
                val virtualPath = when {
                    !_virtualContext.isVirtual -> dotnetPath
                    _virtualContext.targetOSType == OSType.WINDOWS -> Path("dotnet.exe")
                    else -> Path("dotnet")
                }

                return ToolPath(dotnetPath, virtualPath)
            } catch (e: ToolCannotBeFoundException) {
                val exception = RunBuildException(e)
                exception.isLogStacktrace = false
                throw exception
            }
        }

    override val isCommandRequired: Boolean
        get() = true

    private fun tryFinding(parameterType: ParameterType, parameterName: String): Sequence<Path> {
        val dotnetPath = _parametersService.tryGetParameter(parameterType, parameterName)
        LOG.debug("$parameterType variable \"$parameterName\" is \"$dotnetPath\"")
        return if (!dotnetPath.isNullOrBlank()) sequenceOf(Path(dotnetPath)) else emptySequence<Path>()
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetToolResolverImpl::class.java)
    }
}