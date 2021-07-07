package jetbrains.buildServer.dotnet.test.script

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.script.RspContentFactoryImpl
import jetbrains.buildServer.script.ScriptConstants
import jetbrains.buildServer.script.ScriptResolver
import jetbrains.buildServer.script.ScriptType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class RspContentFactoryTest {
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _argumentsService: ArgumentsService
    @MockK private lateinit var _scriptProvider: ScriptResolver
    @MockK private lateinit var _virtualContext: VirtualContext
    private val _tool = File("Tool")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _argumentsService.split(any()) } answers {arg<String>(0).split(' ').asSequence()}
        every { _scriptProvider.resolve() } returns _tool
        every { _virtualContext.resolvePath(any()) } answers {"v_" + arg(0)}
    }

    @Test(dataProvider = "cases")
    fun shouldResolve(
            runnerParameters: Map<String, String>,
            systemParameters: Map<String, String>,
            expectedLines: List<String>) {
        // Given
        val factory = createInstance()
        every { _parametersService.tryGetParameter(ParameterType.Runner, any()) } answers {runnerParameters[arg(1)]}
        every { _parametersService.getParameterNames(ParameterType.System) } returns systemParameters.keys.asSequence()
        every { _parametersService.tryGetParameter(ParameterType.System, any()) } answers {systemParameters[arg(1)]}

        // When
        val actualLines = factory.create().toList()

        // Then
        Assert.assertEquals(actualLines, expectedLines)
    }

    @DataProvider(name = "cases")
    fun getCases(): Array<Array<out Any?>> {
        return arrayOf(
                // Script file
                arrayOf(
                        mapOf(
                                ScriptConstants.NUGET_PACKAGE_SOURCES to "NuGetSrc",
                                ScriptConstants.ARGS to "Arg1"
                        ),
                        mapOf(
                                "Param1" to "Value1"
                        ),
                        listOf(
                                "--source",
                                "v_NuGetSrc",
                                "--property",
                                "Param1=v_Value1",
                                "--",
                                "v_" + _tool.path,
                                "Arg1"
                        )
                )
        )
    }

    private fun createInstance() =
            RspContentFactoryImpl(_parametersService, _argumentsService, _scriptProvider, _virtualContext)
}