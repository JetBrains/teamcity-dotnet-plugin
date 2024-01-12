

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetArgumentsProvider
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.commands.targeting.TargetTypeProvider
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import java.io.File

class TestCommand(
    parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    override val toolResolver: DotnetToolResolver,
    private val _targetService: TargetService,
    private val _commonArgumentsProvider: DotnetCommonArgumentsProvider,
    private val _assemblyArgumentsProvider: DotnetCommonArgumentsProvider,
    private val _dotnetFilterFactory: DotnetFilterFactory,
    private val _targetTypeProvider: TargetTypeProvider,
    private val _targetArgumentsProvider: TargetArgumentsProvider,
    override val environmentBuilders: List<EnvironmentBuilder>
) : DotnetCommandBase(parametersService) {
    override val commandType = DotnetCommandType.Test

    override val command = sequenceOf("test")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetArgumentsProvider.getTargetArguments(_targetService.targets)

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        val filter = _dotnetFilterFactory.createFilter(context)

        if (filter.filter.isNotBlank()) {
            yield(CommandLineArgument("--filter"))
            yield(CommandLineArgument(filter.filter))
        }

        if (filter.settingsFile != null) {
            yield(CommandLineArgument("--settings"))
            yield(CommandLineArgument(filter.settingsFile.path))
        }
        else {
            parameters(DotnetConstants.PARAM_TEST_SETTINGS_FILE)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield(CommandLineArgument("--settings"))
                    yield(CommandLineArgument(it))
                }
            }
        }

        parameters(DotnetConstants.PARAM_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--framework"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--configuration"))
                yield(CommandLineArgument(it))
            }
        }

        parameters(DotnetConstants.PARAM_OUTPUT_DIR)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("--output"))
                yield(CommandLineArgument(it))
            }
        }

        val hasAssemblyTarget = context.command.targetArguments.flatMap { it.arguments }.any { isAssembly(it.value)}
        // no need to specify --no-build argument for .dll only targets
        if (!hasAssemblyTarget && parameters(DotnetConstants.PARAM_SKIP_BUILD, "").trim().toBoolean()) {
            yield(CommandLineArgument("--no-build"))
        }

        if (hasAssemblyTarget) {
            yieldAll(_assemblyArgumentsProvider.getArguments(context))
        }
        else {
            yieldAll(_commonArgumentsProvider.getArguments(context))
        }
    }


    private fun isAssembly(path: String) = _targetTypeProvider.getTargetType(File(path)) == CommandTargetType.Assembly
}