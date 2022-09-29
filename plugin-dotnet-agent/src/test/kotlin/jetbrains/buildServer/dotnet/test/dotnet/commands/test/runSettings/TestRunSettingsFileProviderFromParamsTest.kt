package jetbrains.buildServer.dotnet.test.dotnet.commands.test.runSettings

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsFileProviderFromParams
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestRunSettingsFileProviderFromParamsTest {
    @MockK private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(DotnetCommandType.VSTest, "Abc", File("Abc")),
                arrayOf(DotnetCommandType.Test, "Abc", File("Abc")),

                arrayOf(DotnetCommandType.MSBuild, "Abc", null),
                arrayOf(DotnetCommandType.VSTest, null, null),
                arrayOf(DotnetCommandType.VSTest, "", null),
                arrayOf(DotnetCommandType.VSTest, "  ", null)
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldProvideSettingsFile(command: DotnetCommandType, runnerParamValue: String?, expectedFile: File?) {
        // Given
        val provider = createInstance()

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_TEST_SETTINGS_FILE) } returns runnerParamValue
        val actualFile = provider.tryGet(command)

        // Then
        Assert.assertEquals(actualFile, expectedFile)
    }

    private fun createInstance() = TestRunSettingsFileProviderFromParams(_parametersService)
}