package jetbrains.buildServer.dotnet.commands.custom

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import java.nio.file.Path

class TeamCityDotnetTestSuppressorCommand(
    private val _customCommand: DotnetCommand,
    private val _pathsService: PathsService,
    private val _parameterService: ParametersService,
) : DotnetCommand by _customCommand {
    override val targetArguments: Sequence<TargetArguments> get() = emptySequence()

    override val title = toolPath.fileName.toString()

    override val isAuxiliary: Boolean = true

    override fun getArguments(context: DotnetBuildContext) = sequence {
        // run on higher available version of .NET SDK
        yield(CommandLineArgument("--roll-forward"))
        yield(CommandLineArgument(RollForwardOption.LatestMajor.toString()))

        yield(CommandLineArgument(toolPath.toString(), CommandLineArgumentType.Mandatory))

        yield(CommandLineArgument("--verbosity"))
        yield(CommandLineArgument(verbosity))
    }

    private val toolPath: Path
        get() = _pathsService.resolvePath(PathType.Plugin, RelativeToolPath)

    protected val verbosity get() = when (dotnetVerbosity) {
        Verbosity.Quiet -> "quiet"
        Verbosity.Minimal -> "minimal"
        Verbosity.Normal -> "normal"
        Verbosity.Detailed -> "detailed"
        Verbosity.Diagnostic -> "diagnostic"
    }

    private val dotnetVerbosity get(): Verbosity =
        _parameterService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
            ?.let { Verbosity.tryParse(it) }
            ?: Verbosity.Normal

    companion object {
        private const val ToolName = "TeamCity.Dotnet.Plugin.Agent.AssemblyLevelTestFilter.dll"
        private const val RelativeToolPath = "tools/AssemblyLevelTestFilter/" + ToolName
    }
}