

package jetbrains.buildServer.dotnet.test.dotnet.commands.test.runSettings

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetCommandContext
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.test.runSettings.TestRunSettingsFileProviderFromKeyValueArgs
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class TestRunSettingsFileProviderFromKeyValueArgsTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _commandContext: DotnetCommandContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _argumentsService.split(any()) } answers { arg<String>(0).split("_").asSequence() }
    }

    @DataProvider
    fun testCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(DotnetCommandType.VSTest, "--settings:Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.VSTest, "--SettingS:Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.VSTest, "/settings:Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.VSTest, "/SettingS:Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.VSTest, "--settings:Xyz_--settings:Abc", File("Abc")),
                arrayOf(DotnetCommandType.VSTest, "--settings:Xyz_/Settings:Abc", File("Abc")),
                arrayOf(DotnetCommandType.VSTest, "--settings: Xyz", File("Xyz")),
                arrayOf(DotnetCommandType.VSTest, "--settings:  Xyz ", File("Xyz")),

                arrayOf(DotnetCommandType.Test, "--settings:Xyz", null),
                arrayOf(DotnetCommandType.MSBuild, "--settings:Xyz", null),
                arrayOf(DotnetCommandType.VSTest, null, null),
                arrayOf(DotnetCommandType.VSTest, "", null),
                arrayOf(DotnetCommandType.VSTest, "   ", null),
                arrayOf(DotnetCommandType.VSTest, "--settings:", null),
                arrayOf(DotnetCommandType.VSTest, "-- settings:Abc", null),
                arrayOf(DotnetCommandType.VSTest, "/ settings:Abc", null),
                arrayOf(DotnetCommandType.VSTest, "  -- settings:Abc", null),
        )
    }

    @Test(dataProvider = "testCases")
    public fun shouldProvideSettingsFile(command: DotnetCommandType, runnerParamValue: String?, expectedFile: File?) {
        // Given
        val provider = createInstance()

        // When
        every { _parametersService.tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_ARGUMENTS) } returns runnerParamValue
        every { _commandContext.command.commandType } returns command
        val actualFile = provider.tryGet(_commandContext)

        // Then
        Assert.assertEquals(actualFile, expectedFile)
    }

    private fun createInstance() = TestRunSettingsFileProviderFromKeyValueArgs(listOf("/settings:", "--settings:"), listOf(DotnetCommandType.VSTest), _parametersService, _argumentsService)
}