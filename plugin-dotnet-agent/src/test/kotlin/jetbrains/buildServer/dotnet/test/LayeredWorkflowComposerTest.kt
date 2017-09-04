package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotcover.CoverageFilter
import jetbrains.buildServer.dotcover.CoverageFilterProvider
import jetbrains.buildServer.dotcover.DotCoverProject
import jetbrains.buildServer.dotcover.DotCoverProjectSerializerImpl
import jetbrains.buildServer.runners.*
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Invocation
import org.jmock.lib.action.CustomAction
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream

class LayeredWorkflowComposerTest {
    private val _toolWorkflow: Workflow = Workflow(emptySequence())
    private val _profilerOfCodeCoverageWorkflow: Workflow = Workflow(emptySequence())
    private val _baseWorkflow: Workflow = Workflow(emptySequence())

    @DataProvider(name = "composeCases")
    fun getComposeCases(): Array<Array<Any>> {
        val ctx = Mockery()
        val workflowContext = ctx.mock<WorkflowContext>(WorkflowContext::class.java)
        val toolWorkflowComposer = ctx.mock<WorkflowComposer>(WorkflowComposer::class.java, "Tool")
        val notApplicableWorkflowComposer = ctx.mock<WorkflowComposer>(WorkflowComposer::class.java, "NotApplicable")
        val profilerOfCodeCoverageWorkflowComposer = ctx.mock<WorkflowComposer>(WorkflowComposer::class.java, "ProfilerOfCodeCoverage")
        ctx.checking(object : Expectations() {
            init {
                allowing<WorkflowComposer>(toolWorkflowComposer).target
                will(returnValue(TargetType.Tool))

                allowing<WorkflowComposer>(toolWorkflowComposer).compose(workflowContext!!, _baseWorkflow)
                will(returnValue(_toolWorkflow))

                allowing<WorkflowComposer>(profilerOfCodeCoverageWorkflowComposer).target
                will(returnValue(TargetType.ProfilerOfCodeCoverage))

                allowing<WorkflowComposer>(profilerOfCodeCoverageWorkflowComposer).compose(workflowContext, _toolWorkflow)
                will(returnValue(_profilerOfCodeCoverageWorkflow))

                allowing<WorkflowComposer>(notApplicableWorkflowComposer).target
                will(returnValue(TargetType.NotApplicable))

                never<WorkflowComposer>(notApplicableWorkflowComposer).compose(workflowContext, _toolWorkflow)
                never<WorkflowComposer>(notApplicableWorkflowComposer).compose(workflowContext, _baseWorkflow)
                never<WorkflowComposer>(notApplicableWorkflowComposer).compose(workflowContext, _profilerOfCodeCoverageWorkflow)
            }
        })

        return arrayOf(
                arrayOf(ctx, workflowContext, listOf(notApplicableWorkflowComposer, toolWorkflowComposer, profilerOfCodeCoverageWorkflowComposer) as Any),
                arrayOf(ctx, workflowContext, listOf(profilerOfCodeCoverageWorkflowComposer, notApplicableWorkflowComposer, toolWorkflowComposer) as Any),
                arrayOf(ctx, workflowContext, listOf(toolWorkflowComposer, profilerOfCodeCoverageWorkflowComposer, notApplicableWorkflowComposer) as Any),
                arrayOf(ctx, workflowContext, listOf(profilerOfCodeCoverageWorkflowComposer, toolWorkflowComposer, notApplicableWorkflowComposer) as Any))
    }

    @Test(dataProvider = "composeCases")
    fun shouldCompose(
            ctx: Mockery,
            workflowContext: WorkflowContext,
            composers: List<WorkflowComposer>) {
        // Given

        val composer = createInstance(composers)

        // When
        val actualWorkflow = composer.compose(workflowContext, _baseWorkflow);

        // Then
        ctx.assertIsSatisfied()
        Assert.assertEquals(actualWorkflow, _profilerOfCodeCoverageWorkflow);
    }

    private fun createInstance(composers: List<WorkflowComposer>): WorkflowComposer {
        return LayeredWorkflowComposer(composers)
    }
}