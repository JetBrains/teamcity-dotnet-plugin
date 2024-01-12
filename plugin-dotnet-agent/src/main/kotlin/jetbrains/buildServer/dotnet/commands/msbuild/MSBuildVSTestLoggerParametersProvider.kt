

package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.test.TestReportingParameters
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.logging.LoggerResolver
import java.util.*

class MSBuildVSTestLoggerParametersProvider(
    private val _pathsService: PathsService,
    private val _parameterService: ParametersService,
    private val _loggerResolver: LoggerResolver,
    private val _testReportingParameters: TestReportingParameters,
    private val _loggerParameters: LoggerParameters,
    private val _virtualContext: VirtualContext,
    private val _customArgumentsProvider: ArgumentsProvider
) : MSBuildParametersProvider {
    private val loggerArgumentNames = setOf("--logger", "-l")
    private val isCustomLoggersDisabled // feature flag
        get() =
            _parameterService.tryGetParameter(
                ParameterType.Configuration,
                DotnetConstants.PARAM_MSBUILD_DISABLE_CUSTOM_VSTEST_LOGGERS
            )
                ?.let { it.trim().equals("true", ignoreCase = true) }
                ?: false

    override fun getParameters(context: DotnetCommandContext): Sequence<MSBuildParameter> = sequence {
        val testReportingMode = _testReportingParameters.getMode(context)
        if (testReportingMode.contains(TestReportingMode.Off)) {
            return@sequence
        }

        _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
            yield(getVSTestLoggerParameter(context))

            var paths = _virtualContext.resolvePath(_pathsService.getPath(PathType.Checkout).canonicalPath)
            if (testReportingMode.contains(TestReportingMode.MultiAdapterPath_5_0_103)) {
                paths = ".;${_virtualContext.resolvePath(it.canonicalPath)}"
            } else {
                if (testReportingMode.contains(TestReportingMode.MultiAdapterPath)) {
                    paths = "${_virtualContext.resolvePath(it.canonicalPath)};."
                }
            }

            yield(MSBuildParameter("VSTestTestAdapterPath", paths, MSBuildParameterType.Predefined))
        }

        yield(
            MSBuildParameter(
                "VSTestVerbosity",
                _loggerParameters.vsTestVerbosity.id.lowercase(Locale.getDefault()),
                MSBuildParameterType.Predefined
            )
        )
    }

    private fun getVSTestLoggerParameter(context: DotnetCommandContext): MSBuildParameter {
        val name = "VSTestLogger"
        var value = "logger://teamcity"
        var type = MSBuildParameterType.Predefined

        if (!isCustomLoggersDisabled) {
            val loggerSequence = getLoggerSequence(context)
            value = loggerSequence
                .map { MSBuildParameterNormalizer.normalizeValue(it, true) }
                .joinToString(";")

            type = MSBuildParameterType.Unknown
        }

        return MSBuildParameter(name, value, type)
    }

    private fun getLoggerSequence(context: DotnetCommandContext) = sequence {
        yield("teamcity")

        // find loggers among custom arguments
        val customLoggerSequence = _customArgumentsProvider.getArguments(context)
            .zipWithNext()
            .filter { pair -> loggerArgumentNames.contains(pair.first.value) && !pair.second.value.startsWith("-") }
            .map { pair -> pair.second.value }

        yieldAll(customLoggerSequence)
    }
}