

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.filter
import jetbrains.buildServer.rx.map
import jetbrains.buildServer.rx.observer
import jetbrains.buildServer.rx.use

class DotnetStateWorkflowComposer(
    private val _pathsService: PathsService,
    private val _virtualContext: VirtualContext,
    private val _pathResolverWorkflowComposers: List<PathResolverWorkflowComposer>,
    private val _versionParser: ToolVersionOutputParser,
    private val _defaultEnvironmentVariables: EnvironmentVariables
) : ToolStateWorkflowComposer {
    override val target: TargetType
        get() = TargetType.SystemDiagnostics

    override fun compose(context: WorkflowContext, state: ToolState, workflow: Workflow): Workflow = sequence {
        val executable = state.executable
        var virtualPath: Path? = null
        if (_virtualContext.isVirtual && executable.homePaths.isEmpty()) {
            // Getting dotnet executable
            val pathResolverState = PathResolverState(
                executable.virtualPath,
                observer<Path> {
                    if (virtualPath == null && it.path.isNotBlank()) {
                        virtualPath = it
                        state.virtualPathObserver.onNext(it)
                    }
                }
            )

            for (pathResolverWorkflowFactory in _pathResolverWorkflowComposers) {
                yieldAll(pathResolverWorkflowFactory.compose(context, pathResolverState).commandLines)
            }
        }

        if (state.versionObserver == null) {
            return@sequence
        }

        // Getting .NET SDK version
        val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)
        context
            .toOutput()
            .map { _versionParser.parse(listOf(it)) }
            .filter { it != Version.Empty }
            .subscribe(state.versionObserver)
            .use {
                yield(
                    CommandLine(
                        baseCommandLine = null,
                        target = TargetType.SystemDiagnostics,
                        executableFile = virtualPath ?: executable.virtualPath,
                        workingDirectory = workingDirectory,
                        arguments = listOf(CommandLineArgument("--version")),
                        environmentVariables = _defaultEnvironmentVariables.getVariables(Version.Empty).toList(),
                        title = "dotnet --version",
                        description = listOf(StdOutText("Getting the .NET SDK version"))
                    )
                )
            }
    }.let(::Workflow)
}