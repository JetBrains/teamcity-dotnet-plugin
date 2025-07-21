package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.TempFiles
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetRunnerCacheDirectoryProvider
import jetbrains.buildServer.dotnet.RestorePackagesPathArgumentsProvider
import jetbrains.buildServer.dotnet.RestorePackagesPathManager
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class RestorePackagesPathArgumentsProviderTest {
    @MockK
    private lateinit var _restorePackagesPathManager: RestorePackagesPathManager
    @MockK
    private lateinit var _buildStepContext: BuildStepContext
    private lateinit var tempFiles: TempFiles
    private lateinit var cachesDir: File

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        tempFiles = TempFiles()
        cachesDir = File(tempFiles.createTempDir(), DotnetRunnerCacheDirectoryProvider.DOTNET_CACHE_DIR)

        val runnerContext = mockk<BuildRunnerContext>()
        every { _buildStepContext.runnerContext } returns runnerContext
        val build = mockk<AgentRunningBuild>()
        every { runnerContext.build } returns build
        val agentConfiguration = mockk<BuildAgentConfiguration>()
        every { build.agentConfiguration } returns agentConfiguration
    }

    @Test
    fun `should not override RestorePackagesPath when disabled`() {
        // arrange
        val context = mockk<DotnetCommandContext>()
        every { _restorePackagesPathManager.shouldOverrideRestorePackagesPath() } returns false
        val argumentsProvider = create()

        // act
        val result = argumentsProvider.getArguments(context).toList()

        // assert
        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun `should override RestorePackagesPath`() {
        // arrange
        val context = mockk<DotnetCommandContext>()
        every { _restorePackagesPathManager.shouldOverrideRestorePackagesPath() } returns true
        every { _restorePackagesPathManager.getRestorePackagesPathLocation(any()) } returns cachesDir
        val argumentsProvider = create()

        // act
        val result = argumentsProvider.getArguments(context).toList()

        // assert
        Assert.assertEquals(result.size, 1)
        Assert.assertEquals(result.get(0).value, "-p:RestorePackagesPath=${cachesDir.absolutePath}")
    }

    private fun create() = RestorePackagesPathArgumentsProvider(
        _restorePackagesPathManager, _buildStepContext
    )
}