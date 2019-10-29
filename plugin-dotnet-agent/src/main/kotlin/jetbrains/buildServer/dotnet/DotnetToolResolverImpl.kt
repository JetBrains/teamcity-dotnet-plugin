package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.util.OSType
import org.apache.log4j.Logger

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
                val homePaths = _toolEnvironment.homePaths.toList()
                val executables = _toolSearchService.find(DotnetConstants.EXECUTABLE, _toolEnvironment.homePaths).map { Path(it.path) } + tryFinding(ParameterType.Configuration, DotnetConstants.CONFIG_PATH)
                var dotnetPath = executables.firstOrNull()
                if (dotnetPath == null) {
                    if(_virtualContext.isVirtual) {
                        dotnetPath = getHomePath(_environment.os, homePaths)
                    }
                    else {
                        throw RunBuildException("Cannot find the ${DotnetConstants.EXECUTABLE} executable.")
                    }
                }

                val virtualPath = when {
                    _virtualContext.isVirtual -> getHomePath(_virtualContext.targetOSType, homePaths)
                    else -> dotnetPath
                }

                return ToolPath(dotnetPath, virtualPath, homePaths)
            } catch (e: ToolCannotBeFoundException) {
                val exception = RunBuildException(e)
                exception.isLogStacktrace = false
                throw exception
            }
        }

    override val isCommandRequired: Boolean
        get() = true

    private fun getHomePath(os: OSType, homePaths: List<Path>) = when {
        homePaths.isNotEmpty() -> Path("${homePaths[0].path}${separator(os)}${defaultExecutable(os).path}")
        else -> defaultExecutable(os)
    }

    private fun separator(os: OSType) = when {
        os == OSType.WINDOWS -> '\\'
        else -> '/'
    }

    private fun defaultExecutable(os: OSType) = when {
        os == OSType.WINDOWS -> Path("dotnet.exe")
        else -> Path("dotnet")
    }

    private fun tryFinding(parameterType: ParameterType, parameterName: String): Sequence<Path> {
        val dotnetPath = _parametersService.tryGetParameter(parameterType, parameterName)
        LOG.debug("$parameterType variable \"$parameterName\" is \"$dotnetPath\"")
        return if (!dotnetPath.isNullOrBlank()) sequenceOf(Path(dotnetPath)) else emptySequence<Path>()
    }

    companion object {
        private val LOG = Logger.getLogger(DotnetToolResolverImpl::class.java)
    }
}