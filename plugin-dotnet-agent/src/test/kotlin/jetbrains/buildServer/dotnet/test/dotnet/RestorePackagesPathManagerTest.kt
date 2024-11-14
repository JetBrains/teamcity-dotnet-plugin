package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.TempFiles
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetRunnerCacheDirectoryProvider
import jetbrains.buildServer.dotnet.RestorePackagesPathManager
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File

class RestorePackagesPathManagerTest {
    @MockK
    private lateinit var _parametersService: ParametersService
    @MockK
    private lateinit var _dotnetRunnerCacheDirectoryProvider: DotnetRunnerCacheDirectoryProvider
    @MockK
    private lateinit var agentConfiguration: BuildAgentConfiguration
    private lateinit var tempFiles: TempFiles
    private lateinit var cachesDir: File

    @BeforeMethod
    fun setup(){
        clearAllMocks()
        MockKAnnotations.init(this)
        tempFiles = TempFiles()
        cachesDir = File(tempFiles.createTempDir(), DotnetRunnerCacheDirectoryProvider.DOTNET_CACHE_DIR)

        every { _dotnetRunnerCacheDirectoryProvider.getDotnetRunnerCacheDirectory(any()) } returns cachesDir
    }

    @Test
    fun `should override RestorePackagesPath when config param is set`() {
        // arrange
        every {_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.RESTORE_PACKAGES_PATH_OVERRIDE_ENABLED) } returns "true"
        val restorePackagesPathManager = create()

        // act
        val result = restorePackagesPathManager.shouldOverrideRestorePackagesPath()

        // assert
        Assert.assertTrue(result)
    }

    @Test
    fun `should override RestorePackagesPath when config param is null`() {
        // arrange
        every {_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.RESTORE_PACKAGES_PATH_OVERRIDE_ENABLED) } returns null
        val restorePackagesPathManager = create()

        // act
        val result = restorePackagesPathManager.shouldOverrideRestorePackagesPath()

        // assert
        Assert.assertTrue(result)
    }

    @Test
    fun `should not override RestorePackagesPath when config param is false`() {
        // arrange
        every {_parametersService.tryGetParameter(ParameterType.Configuration, DotnetConstants.RESTORE_PACKAGES_PATH_OVERRIDE_ENABLED) } returns "false"
        val restorePackagesPathManager = create()

        // act
        val result = restorePackagesPathManager.shouldOverrideRestorePackagesPath()

        // assert
        Assert.assertFalse(result)
    }

    @Test
    fun `should return restore packages path location`() {
        // arrange
        val restorePackagesPathManager = create()

        // act
        val result = restorePackagesPathManager.getRestorePackagesPathLocation(agentConfiguration)

        // assert
        Assert.assertEquals(result, File(cachesDir, RestorePackagesPathManager.NUGET_PACKAGES_DIR))
    }

    private fun create() = RestorePackagesPathManager(
        _parametersService, _dotnetRunnerCacheDirectoryProvider
    )
}