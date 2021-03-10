package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.CltConstants.CLT_PATH_PARAMETER
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.inspect.InspectCodeConstants.RUNNER_SETTING_CLT_PLATFORM_X86_PARAMETER
import jetbrains.buildServer.inspect.InspectionTool
import jetbrains.buildServer.inspect.ToolPathResolverImpl
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ToolPathResolverTest {
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0)}
    }

    @DataProvider
    fun resolveCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to RUNNER_SETTING_CLT_PLATFORM_X86_PARAMETER)),
                        OSType.WINDOWS,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.x86.exe"),
                        null
                ),
                arrayOf(
                        InspectionTool.Dupfinder,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to RUNNER_SETTING_CLT_PLATFORM_X86_PARAMETER)),
                        OSType.WINDOWS,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Dupfinder.toolName).path}.x86.exe"),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to "Abc")),
                        OSType.WINDOWS,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe"),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.WINDOWS,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe"),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.UNIX,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh"),
                        null
                ),
                arrayOf(
                        InspectionTool.Dupfinder,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.UNIX,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Dupfinder.toolName).path}.sh"),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.MAC,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh"),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(RUNNER_SETTING_CLT_PLATFORM to RUNNER_SETTING_CLT_PLATFORM_X86_PARAMETER)),
                        OSType.WINDOWS,
                        Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.x86.exe"),
                        RunBuildException("Cannot find ${InspectionTool.Inspectcode.dysplayName}.")
                )
        )
    }

    @Test(dataProvider = "resolveCases")
    fun shouldResolve(
            tool: InspectionTool,
            parametersService: ParametersService,
            os: OSType,
            expectedPath: Path,
            expectedException: RunBuildException?) {
        // Given
        var actualPath: Path? = null
        val resolver = createInstance(parametersService)
        every { _virtualContext.targetOSType } returns os

        // When
        try {
            actualPath = resolver.resolve(tool)
        }
        catch (actualException: RunBuildException) {
            Assert.assertEquals(actualException.message, expectedException?.message)
        }

        // Then
        if(expectedException == null) {
            Assert.assertEquals(actualPath, expectedPath)
        }
    }

    private fun createInstance(parametersService: ParametersService) =
            ToolPathResolverImpl(parametersService, _virtualContext)
}