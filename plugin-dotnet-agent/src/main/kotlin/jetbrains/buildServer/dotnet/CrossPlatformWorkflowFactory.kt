package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.rx.*

class CrossPlatformWorkflowFactory(
        private val _pathsService: PathsService,
        private val _virtualContext: VirtualContext,
        private val _pathResolverWorkflowFactories: List<PathResolverWorkflowFactory>,
        private val _versionParser: VersionParser,
        private val _defaultEnvironmentVariables: EnvironmentVariables)
    : WorkflowFactory<CrossPlatformWorkflowState> {
    override fun create(context: WorkflowContext, state: CrossPlatformWorkflowState): Workflow = Workflow(
            sequence {
                val executable = state.executable
                var virtualPath: Path? = null
                val workingDirectory = Path(_pathsService.getPath(PathType.WorkingDirectory).canonicalPath)
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

                    for (pathResolverWorkflowFactory in _pathResolverWorkflowFactories) {
                        yieldAll(pathResolverWorkflowFactory.create(context, pathResolverState).commandLines)
                    }
                }

                // Getting .NET Core version
                context
                        .toOutput()
                        .map { _versionParser.parse(listOf(it)) }
                        .filter { it != Version.Empty }
                        .subscribe(state.versionObserver)
                        .use {
                            yield(
                                    CommandLine(
                                            null,
                                        TargetType.SystemDiagnostics,
                                        virtualPath ?: executable.virtualPath,
                                        workingDirectory,
                                        DotnetWorkflowComposer.VersionArgs,
                                        _defaultEnvironmentVariables.getVariables(Version.Empty).toList(),
                                    "dotnet --version",
                                        listOf(StdOutText("Getting the .NET SDK version"))))
                        }
            }
    )
}