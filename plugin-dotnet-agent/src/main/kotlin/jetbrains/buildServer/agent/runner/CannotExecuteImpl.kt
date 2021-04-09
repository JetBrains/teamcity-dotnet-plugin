package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.sh.ShWorkflowComposer

class CannotExecuteImpl(
        private val _virtualContext: VirtualContext,
        private val _loggerService: LoggerService)
    : CannotExecute {
    override fun writeBuildProblemFor(executablePath: Path) =
        _loggerService.writeBuildProblem(
                CannotExecuteProblemId,
                "Cannot execute",
                if (_virtualContext.isVirtual)
                    "Cannot execute \"$executablePath\". Try to use a different Docker image for this build."
                else
                    "Cannot execute \"$executablePath\". Try to adjust agent requirements and run the build on a different agent."
        )

    companion object {
        internal const val CannotExecuteProblemId = "Cannot execute"
    }
}