package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.*
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test

class CannotExecuteTest {
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _loggerService: LoggerService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldWriteBuildProblem() {
        // Given
        val cannotExecute = createInstance()
        every { _loggerService.writeBuildProblem(any(), any(), any()) } returns Unit
        every { _virtualContext.isVirtual } returns false

        // When
        cannotExecute.writeBuildProblemFor(Path("abc"))

        // Then
        verify { _loggerService.writeBuildProblem(CannotExecuteImpl.CannotExecuteProblemId, "Cannot execute", "Cannot execute \"abc\". Try to adjust agent requirements and run the build on a different agent.") }
    }

    @Test
    fun shouldWriteBuildProblemWhenVirtualContext() {
        // Given
        val cannotExecute = createInstance()
        every { _loggerService.writeBuildProblem(any(), any(), any()) } returns Unit
        every { _virtualContext.isVirtual } returns true

        // When
        cannotExecute.writeBuildProblemFor(Path("abc"))

        // Then
        verify { _loggerService.writeBuildProblem(CannotExecuteImpl.CannotExecuteProblemId, "Cannot execute", "Cannot execute \"abc\". Try to use a different Docker image for this build.") }
    }

    private fun createInstance() =
            CannotExecuteImpl(_virtualContext, _loggerService)
}