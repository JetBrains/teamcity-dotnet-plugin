

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter
import jetbrains.buildServer.dotnet.commands.responseFile.ResponseFileFactory
import jetbrains.buildServer.dotnet.commands.targeting.TargetArguments
import jetbrains.buildServer.dotnet.commands.targeting.TargetService
import jetbrains.buildServer.dotnet.commands.targeting.TargetsParser

class MSBuildCommand(
    _parametersService: ParametersService,
    override val resultsAnalyzer: ResultsAnalyzer,
    private val _targetService: TargetService,
    private val _msBuildResponseFileArgumentsProvider: ArgumentsProvider,
    private val _customArgumentsProvider: ArgumentsProvider,
    override val toolResolver: ToolResolver,
    private val _targetsParser: TargetsParser,
    private val _dotnetFilterFactory: DotnetFilterFactory,
    private val _responseFileFactory: ResponseFileFactory,
    override val environmentBuilders: List<EnvironmentBuilder>
) : DotnetCommandBase(_parametersService) {

    override val commandType = DotnetCommandType.MSBuild

    override val command = sequenceOf("msbuild")

    override val targetArguments: Sequence<TargetArguments>
        get() = _targetService.targets.map { TargetArguments(sequenceOf(CommandLineArgument(it.target.path, CommandLineArgumentType.Target))) }

    override fun getArguments(context: DotnetCommandContext): Sequence<CommandLineArgument> = sequence {
        val filter = _dotnetFilterFactory.createFilter(context);

        parameters(DotnetConstants.PARAM_TARGETS)?.trim()?.let {
            val targets = _targetsParser.parse(it)
            if (targets.isNotBlank()) {
                yield(CommandLineArgument("-t:$targets"))
            }
        }

        parameters(DotnetConstants.PARAM_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("-p:Configuration=$it"))
            }
        }

        parameters(DotnetConstants.PARAM_PLATFORM)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("-p:Platform=$it"))
            }
        }

        parameters(DotnetConstants.PARAM_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(CommandLineArgument("-p:RuntimeIdentifiers=$it"))
            }
        }

        context.verbosityLevel?.let {
            yield(CommandLineArgument("-v:${it.id.lowercase()}"))
        }

        yieldAll(_msBuildResponseFileArgumentsProvider.getArguments(context))

        if (filter.isNotEmpty())
        {
            val msBuildParams = mutableListOf<MSBuildParameter>()
            if (filter.filter.isNotBlank()) {
                msBuildParams.add(MSBuildParameter("VSTestTestCaseFilter", filter.filter))
            }

            if (filter.settingsFile != null) {
                msBuildParams.add(MSBuildParameter("VSTestSetting", filter.settingsFile.path))
            }

            if (msBuildParams.any()) {
                val filterResponseFile = _responseFileFactory.createResponeFile("Filter", emptySequence(), msBuildParams.asSequence(), context.verbosityLevel)
                yield(CommandLineArgument("@${filterResponseFile.path}"))
            }
        }

        yieldAll(_customArgumentsProvider.getArguments(context))
    }
}