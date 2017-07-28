package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.dotnet.arguments.*
import jetbrains.buildServer.runners.*
import java.io.File
import kotlin.coroutines.experimental.buildSequence

class DotnetWorkflowComposer(
        private val _parametersService: ParametersService,
        private val _pathsService: PathsService,
        private val _defaultEnvironmentVariables: EnvironmentVariables,
        _buildArgumentsProvider: BuildArgumentsProvider,
        _packArgumentsProvider: PackArgumentsProvider,
        _publishArgumentsProvider: PublishArgumentsProvider,
        _restoreArgumentsProvider: RestoreArgumentsProvider,
        _runArgumentsProvider: RunArgumentsProvider,
        _testArgumentsProvider: TestArgumentsProvider,
        _nugetPushArgumentsProvider: NugetPushArgumentsProvider,
        _nugetDeleteArgumentsProvider: NugetDeleteArgumentsProvider) : WorkflowComposer {

    private val myArgumentsProviders: Map<String, ArgumentsProvider> = mapOf(
            DotnetConstants.COMMAND_BUILD to _buildArgumentsProvider,
            DotnetConstants.COMMAND_PACK to _packArgumentsProvider,
            DotnetConstants.COMMAND_PUBLISH to _publishArgumentsProvider,
            DotnetConstants.COMMAND_RESTORE to _restoreArgumentsProvider,
            DotnetConstants.COMMAND_RUN to _runArgumentsProvider,
            DotnetConstants.COMMAND_TEST to _testArgumentsProvider,
            DotnetConstants.COMMAND_NUGET_PUSH to _nugetPushArgumentsProvider,
            DotnetConstants.COMMAND_NUGET_DELETE to _nugetDeleteArgumentsProvider)

    override val target: TargetType
        get() = TargetType.Tool

    override fun compose(context: WorkflowContext, workflow: Workflow): Workflow {
        if(workflow.commandLines.any()) {
            throw RunBuildException("This composer should be a root")
        }

        val commandName = _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_COMMAND)
        if (commandName.isNullOrBlank()) {
            throw RunBuildException("Dotnet command name is empty")
        }

        val argumentsProvider = myArgumentsProviders[commandName] ?: throw RunBuildException("Unable to construct arguments for dotnet command $commandName")

        val toolPath: File
        try {
            toolPath = _pathsService.getToolPath(DotnetConstants.RUNNER_TYPE)
        }
        catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        return Workflow(
                buildSequence {
                    yield(
                            CommandLine(
                                TargetType.Tool,
                                toolPath,
                                _pathsService.getPath(PathType.WorkingDirectory),
                                argumentsProvider.getArguments(),
                                _defaultEnvironmentVariables.variables))

                    if(!context.lastResult.isCompleted) {
                        return@buildSequence
                    }

                    // var exitCode = context.lastResult.exitCode
                    // var standardOutput = context.lastResult.standardOutput.toList()
                    // var errorOutput = context.lastResult.errorOutput.toList()
                }
        )
    }
}