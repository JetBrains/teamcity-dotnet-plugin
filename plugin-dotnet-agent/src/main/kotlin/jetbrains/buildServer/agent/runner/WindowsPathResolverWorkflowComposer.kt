package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.OSType
import java.io.File
import java.io.OutputStreamWriter

class WindowsPathResolverWorkflowComposer(
    private val _pathsService: PathsService,
    private val _fileSystemService: FileSystemService,
    private val _virtualContext: VirtualContext,
    private val _baseResolverWorkflowComposer: PathResolverWorkflowComposer,
) : PathResolverWorkflowComposer {
    override fun compose(context: WorkflowContext, state: PathResolverState, workflow: Workflow) = when {
        context.status == WorkflowStatus.Running && onWindows -> {
            val command = Path(_pathsService.getTempFileName("where.cmd").path)
            _fileSystemService.write(File(command.path)) {
                OutputStreamWriter(it).use {
                    it.write(commandToResolve)
                }
            }

            _baseResolverWorkflowComposer.compose(
                context = context,
                state = PathResolverState(state.pathToResolve, state.virtualPathObserver, command)
            )
        }

        else -> workflow
    }

    private val onWindows get() = _virtualContext.targetOSType == OSType.WINDOWS

    companion object {
        internal val commandToResolve = "@for %%A in (\"%path:;=\";\"%\") do @(@if exist %%~A\\%1 (@echo %%~A\\%1))"
    }
}