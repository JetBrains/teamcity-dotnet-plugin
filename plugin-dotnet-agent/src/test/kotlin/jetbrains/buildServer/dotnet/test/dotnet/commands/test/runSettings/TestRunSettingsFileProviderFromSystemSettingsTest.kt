package jetbrains.buildServer.dotnet.test.dotnet.commands.test.runSettings

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsFileProviderFromSystemSettings
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestRunSettingsFileProviderFromSystemSettingsTest {
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
                arrayOf(DotnetCommandType.MSBuild, "Abc", File("Abc")),

                arrayOf(DotnetCommandType.VSTest, null, null),
                arrayOf(DotnetCommandType.VSTest, "", null),
                arrayOf(DotnetCommandType.VSTest, "  ", null)
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldProvideSettingsFile(command: DotnetCommandType, systemParamValue: String?, expectedFile: File?) {
        // Given
        val provider = createInstance()

        // When
        every { _parametersService.tryGetParameter(ParameterType.System, TestRunSettingsFileProviderFromSystemSettings.SettingSystemParamName) } returns systemParamValue
        val actualFile = provider.tryGet(command)

        // Then
        Assert.assertEquals(actualFile, expectedFile)
    }

    private fun createInstance() = TestRunSettingsFileProviderFromSystemSettings(_parametersService)
}