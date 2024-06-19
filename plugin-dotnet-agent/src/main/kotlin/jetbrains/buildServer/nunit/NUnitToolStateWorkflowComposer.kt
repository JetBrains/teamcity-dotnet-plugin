package jetbrains.buildServer.nunit

import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.nunit.arguments.NUnitConsoleRunnerPathProvider
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import jetbrains.buildServer.nunit.toolState.NUnitToolStateParser
import jetbrains.buildServer.rx.*
import kotlin.io.path.absolutePathString

class NUnitToolStateWorkflowComposer(
    private val _pathsService: PathsService,
    private val _consoleRunnerPathProvider: NUnitConsoleRunnerPathProvider,
    private val _nUnitToolStateParser: NUnitToolStateParser
) : WorkflowComposer<Observer<NUnitToolState>> {
    override val target = TargetType.SystemDiagnostics

    override fun compose(context: WorkflowContext, state: Observer<NUnitToolState>, workflow: Workflow) = sequence {
        val commandLine = createCommandLine()
        var exitCode = 0
        val stdOut = mutableListOf<String>()

        disposableOf(
            context
                .filter { it.SourceId == commandLine.Id }
                .toOutput()
                .filter { it.isNotEmpty() }
                .subscribe { stdOut.add(it) },
            context
                .filter { it.SourceId == commandLine.Id }
                .toExitCodes()
                .subscribe { exitCode = it },
            disposableOf {
                state.onNext(_nUnitToolStateParser.parse(exitCode, stdOut))
            }
        ).use {
            yield(commandLine)
        }

    }.let(::Workflow)

    private fun createCommandLine() = CommandLine(
        baseCommandLine = null,
        target = TargetType.SystemDiagnostics,
        executableFile = Path(_consoleRunnerPathProvider.consoleRunnerPath.absolutePathString()),
        workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).path),
        arguments = listOf(
            CommandLineArgument(LIST_EXTENSIONS_CMD_ARG),
            CommandLineArgument(TEAMCITY_CMD_ARG)
        )
    )

    companion object {
        private const val LIST_EXTENSIONS_CMD_ARG = "--list-extensions"
        private const val TEAMCITY_CMD_ARG = "--teamcity"
    }
}