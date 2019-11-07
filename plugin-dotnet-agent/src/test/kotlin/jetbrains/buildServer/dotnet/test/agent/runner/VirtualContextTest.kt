package jetbrains.buildServer.dotnet.test.agent.runner

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.TargetType
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.agent.runner.VirtualContextImpl
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VirtualContextTest {
    @MockK private lateinit var _baseVirtualContext: VirtualContext
    private lateinit var _buildStepContext: BuildStepContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        _buildStepContext = mockk<BuildStepContext> {
            every { runnerContext } returns mockk<BuildRunnerContext> {
                every { virtualContext } returns _baseVirtualContext
            }
        }
    }

    @Test
    fun shouldPassCanonicalPathToResolveVirtualPath() {
        // Given
        val virtualContext = VirtualContextImpl(_buildStepContext)
        every { _baseVirtualContext.isVirtual } returns true

        // When
        every { _baseVirtualContext.resolvePath(File("originalPath").canonicalPath) } returns("resolvedPath")
        var actualResolvedPath = virtualContext.resolvePath("originalPath")

        // Then
        Assert.assertEquals(actualResolvedPath, "resolvedPath")
    }

    @Test
    fun shouldReturnOriginalPathWhenException() {
        // Given
        val virtualContext = VirtualContextImpl(_buildStepContext)
        every { _baseVirtualContext.isVirtual } returns true

        // When
        every { _baseVirtualContext.resolvePath(any()) } throws Exception("Some error")
        var actualResolvedPath = virtualContext.resolvePath("oroginalPath")

        // Then
        Assert.assertEquals(actualResolvedPath, "oroginalPath")
    }

    @Test
    fun shouldReturnOriginalPathWhenNonVirtual() {
        // Given
        val virtualContext = VirtualContextImpl(_buildStepContext)

        // When
        every { _baseVirtualContext.isVirtual } returns true
        var actualResolvedPath = virtualContext.resolvePath("oroginalPath")

        // Then
        Assert.assertEquals(actualResolvedPath, "oroginalPath")
    }

    @Test
    fun shouldNotChangePathWhenItWasNotActuallyChanged() {
        // Given
        val virtualContext = VirtualContextImpl(_buildStepContext)
        every { _baseVirtualContext.isVirtual } returns true

        // When
        every { _baseVirtualContext.resolvePath(File("originalPath").canonicalPath) } returns(File("originalPath").canonicalPath)
        var actualResolvedPath = virtualContext.resolvePath("originalPath")

        // Then
        Assert.assertEquals(actualResolvedPath, "originalPath")
    }
}