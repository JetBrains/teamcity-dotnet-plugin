

package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jetbrains.buildServer.agent.CommandLine
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.runner.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class LayeredWorkflowComposerTest {
    private val _toolWorkflow: Workflow = Workflow(emptySequence())
    private val _profilerOfCodeCoverageWorkflow: Workflow = Workflow(sequenceOf(CommandLine(null, TargetType.Tool, Path("tool"), Path("wd"), emptyList(), emptyList())))
    private val _baseWorkflow: Workflow = Workflow(emptySequence())

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        // Given
        val workflowContext = mockk<WorkflowContext>()
        val toolWorkflowComposer = mockk<SimpleWorkflowComposer>()
        val notApplicableWorkflowComposer = mockk<SimpleWorkflowComposer>()
        val profilerOfCodeCoverageWorkflowComposer = mockk<SimpleWorkflowComposer>()

        // When
        every { toolWorkflowComposer.target } returns TargetType.Tool
        every { toolWorkflowComposer.compose(workflowContext, Unit, _baseWorkflow) } returns _toolWorkflow
        every { profilerOfCodeCoverageWorkflowComposer.target } returns TargetType.CodeCoverageProfiler
        every { profilerOfCodeCoverageWorkflowComposer.compose(workflowContext, Unit, _toolWorkflow) } returns _profilerOfCodeCoverageWorkflow
        every { notApplicableWorkflowComposer.target } returns TargetType.NotApplicable

        verify(exactly = 0) { notApplicableWorkflowComposer.compose(workflowContext, Unit, _toolWorkflow) }
        verify(exactly = 0) { notApplicableWorkflowComposer.compose(workflowContext, Unit, _toolWorkflow) }
        verify(exactly = 0) { notApplicableWorkflowComposer.compose(workflowContext, Unit, _baseWorkflow) }
        verify(exactly = 0) { notApplicableWorkflowComposer.compose(workflowContext, Unit, _profilerOfCodeCoverageWorkflow) }

        return arrayOf(
                arrayOf(workflowContext, listOf(notApplicableWorkflowComposer, toolWorkflowComposer, profilerOfCodeCoverageWorkflowComposer) as Any, sequenceOf(_profilerOfCodeCoverageWorkflow)),
                arrayOf(workflowContext, listOf(profilerOfCodeCoverageWorkflowComposer, notApplicableWorkflowComposer, toolWorkflowComposer) as Any, sequenceOf(_profilerOfCodeCoverageWorkflow)),
                arrayOf(workflowContext, listOf(toolWorkflowComposer, profilerOfCodeCoverageWorkflowComposer, notApplicableWorkflowComposer) as Any, sequenceOf(_profilerOfCodeCoverageWorkflow)),
                arrayOf(workflowContext, listOf(toolWorkflowComposer, profilerOfCodeCoverageWorkflowComposer, toolWorkflowComposer, notApplicableWorkflowComposer) as Any, sequenceOf(_profilerOfCodeCoverageWorkflow, _profilerOfCodeCoverageWorkflow)),
                arrayOf(workflowContext, listOf(profilerOfCodeCoverageWorkflowComposer, toolWorkflowComposer, notApplicableWorkflowComposer) as Any, sequenceOf(_profilerOfCodeCoverageWorkflow)))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            workflowContext: WorkflowContext,
            composers: List<SimpleWorkflowComposer>,
            expectedWorkflows: Sequence<Workflow>) {
        // Given
        val composer = createInstance(composers)

        // When
        val actualWorkflow = composer.compose(workflowContext, Unit, _baseWorkflow)

        // Then
        Assert.assertEquals(actualWorkflow.commandLines.toList(), expectedWorkflows.flatMap { it.commandLines }.toList())
    }

    private fun createInstance(composers: List<SimpleWorkflowComposer>): SimpleWorkflowComposer {
        return LayeredWorkflowComposer(composers, listOf())
    }
}