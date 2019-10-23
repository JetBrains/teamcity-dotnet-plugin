package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.VSTestLoggerEnvironmentBuilder
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.Observer
import jetbrains.buildServer.util.OSType
import java.io.File
import java.io.OutputStreamWriter
import java.lang.Exception
import java.util.*

class UnixPathResolverWorkflowFactory(
        private val _pathsService: PathsService,
        private val _fileSystemService: FileSystemService,
        private val _virtualContext: VirtualContext,
        private val _baseResolverWorkflowFactory: PathResolverWorkflowFactory)
    : PathResolverWorkflowFactory {
    override fun create(context: WorkflowContext, state: PathResolverState) =
                if (context.status == WorkflowStatus.Running && _virtualContext.targetOSType != OSType.WINDOWS) {
                    _baseResolverWorkflowFactory.create(context, PathResolverState(state.pathToResolve, state, commandToResolve))
                }
                else {
                    Workflow()
                }

    companion object {
        internal val commandToResolve = Path("which")
    }
}