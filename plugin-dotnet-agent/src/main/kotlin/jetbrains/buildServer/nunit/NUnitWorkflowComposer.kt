package jetbrains.buildServer.nunit

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import jetbrains.buildServer.nunit.toolState.NUnitToolStateVerifier
import jetbrains.buildServer.rx.disposableOf
import jetbrains.buildServer.rx.observer
import jetbrains.buildServer.rx.subscribe
import jetbrains.buildServer.rx.use

class NUnitWorkflowComposer(
    private val _buildStepContext: BuildStepContext,
    private val _loggerService: LoggerService,
    private val _nUnitSettings: NUnitSettings,
    private val _nUnitToolStateVerifier: NUnitToolStateVerifier,
    private val _nUnitToolStateComposer: NUnitToolStateWorkflowComposer,
    private val _nUnitViaCommandLineComposer: NUnitViaCommandLineWorkflowComposer,
    private val _nUnitViaProjectFileComposer: NUnitViaProjectFileWorkflowComposer,
    private val _nUnitTestReorderingComposer: NUnitReorderingWorkflowComposer,
    private val _monoExecutableWorkflowComposer: MonoExecutableWorkflowComposer
) : SimpleWorkflowComposer {
    override val target: TargetType = TargetType.Tool

    override fun compose(context: WorkflowContext, state: Unit, workflow: Workflow): Workflow = sequence {
        if (_buildStepContext.runnerContext.runType != NUnitRunnerConstants.NUNIT_RUN_TYPE) {
            return@sequence
        }

        var nUnitToolState: NUnitToolState? = null
        for (command in getCommandLines(context)) {
            if (context.status != WorkflowStatus.Running) {
                break
            }

            if (nUnitToolState == null) {
                yieldAll(_nUnitToolStateComposer.compose(context, observer { nUnitToolState = it }, workflow).commandLines)
                nUnitToolState?.let { _nUnitToolStateVerifier.verify(it) }
            }

            disposableOf(
                context.toExitCodes().subscribe { exitCode ->
                    if (hasFailedTests(exitCode)) {
                        _loggerService.writeWarning("NUnit process finished with positive exit code $exitCode (some tests have failed).")
                    } else if (isFailed(exitCode)) {
                        _loggerService.writeErrorOutput("NUnit process finished with negative exit code $exitCode (unexpected failure).")
                        if (_buildStepContext.runnerContext.build.failBuildOnExitCode) {
                            _loggerService.writeBuildProblem(
                                "nunit_exit_code$exitCode",
                                BuildProblemData.TC_EXIT_CODE_TYPE,
                                "Process exited with code $exitCode"
                            )
                            context.abort(BuildFinishedStatus.FINISHED_WITH_PROBLEMS)
                        }
                    }
                },
                context.subscribe(NUnitOutputDeduplicationObserver())
            ).use {
                yield(command)
            }
        }
    }.let { _monoExecutableWorkflowComposer.compose(context, state, Workflow(it)) }

    private fun getCommandLines(context: WorkflowContext): Sequence<CommandLine> {
        val baseWorkflow = rootComposer.compose(context, Unit)

        return when {
            _nUnitSettings.testReorderingEnabled && _nUnitSettings.testReorderingRecentlyFailedTests.isNotEmpty() -> {
                _nUnitTestReorderingComposer.compose(context, Unit, baseWorkflow).commandLines
            }

            else -> baseWorkflow.commandLines
        }
    }

    private val rootComposer: SimpleWorkflowComposer
        get() = when (_nUnitSettings.useProjectFile) {
            true -> _nUnitViaProjectFileComposer
            false -> _nUnitViaCommandLineComposer
        }

    private fun hasFailedTests(exitCode: Int): Boolean = exitCode > 0
    private fun isFailed(exitCode: Int): Boolean = exitCode < 0
}