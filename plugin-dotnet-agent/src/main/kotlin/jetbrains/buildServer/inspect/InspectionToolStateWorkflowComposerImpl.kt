package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.map
import jetbrains.buildServer.rx.use

class InspectionToolStateWorkflowComposerImpl(
    private val _tool: InspectionTool,
    private val _pathsService: PathsService,
    private val _versionParser: ToolVersionOutputParser
) : InspectionToolStateWorkflowComposer {
    override fun compose(context: WorkflowContext, state: InspectionToolState, workflow: Workflow): Workflow = sequence {
        context
            .toOutput()
            .map { _versionParser.parse(listOf(it)) }
            .filter { !it.isEmpty() }
            .subscribe(state.versionObserver)
            .use {
                yield(
                    CommandLine(
                        baseCommandLine = null,
                        target = TargetType.SystemDiagnostics,
                        executableFile = state.toolStartInfo.executable,
                        workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath),
                        arguments = state.toolStartInfo.arguments.plus(CommandLineArgument("--version")),
                        environmentVariables = emptyList(),
                        title = "Getting ${_tool.displayName} version"
                    )
                )
            }
    }.let(::Workflow)
}