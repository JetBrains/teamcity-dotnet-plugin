package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.CltConstants.CLT_PATH_PARAMETER
import jetbrains.buildServer.inspect.CltConstants.RUNNER_SETTING_CLT_PLATFORM
import jetbrains.buildServer.inspect.InspectionProcess
import jetbrains.buildServer.inspect.InspectionTool
import jetbrains.buildServer.inspect.IspectionToolPlatform
import jetbrains.buildServer.inspect.ProcessResolverImpl
import jetbrains.buildServer.util.OSType
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ProcessResolverTest {
    @MockK
    private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
    }

    @DataProvider
    fun resolveCases(): Array<Array<out Any?>> {
        return arrayOf(
                // Windows
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.X86.id)),
                        OSType.WINDOWS,
                        InspectionProcess (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.x86.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Dupfinder,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.X86.id)),
                        OSType.WINDOWS,
                        InspectionProcess (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Dupfinder.toolName).path}.x86.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to "Abc")),
                        OSType.WINDOWS,
                        InspectionProcess(
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.WINDOWS,
                        InspectionProcess (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.CrossPlatform.id)),
                        OSType.WINDOWS,
                        InspectionProcess (
                                Path(""),
                                listOf(
                                        CommandLineArgument("exec"),
                                        CommandLineArgument("--runtimeconfig"),
                                        CommandLineArgument("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.runtimeconfig.json"),
                                        CommandLineArgument("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.exe")
                                )
                        ),
                        null
                ),

                // Unix, Mac
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.CrossPlatform.id)),
                        OSType.UNIX,
                        InspectionProcess (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.X64.id)),
                        OSType.UNIX,
                        InspectionProcess (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(
                                CLT_PATH_PARAMETER to "somePath",
                                RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.X86.id)),
                        OSType.UNIX,
                        InspectionProcess (
                                Path("v_${File(File(File("somePath"), "tools"), InspectionTool.Inspectcode.toolName).path}.sh")
                        ),
                        null
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.UNIX,
                        null,
                        RunBuildException("Cannot run ${InspectionTool.Inspectcode.dysplayName}.")
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(CLT_PATH_PARAMETER to "somePath")),
                        OSType.MAC,
                        null,
                        RunBuildException("Cannot run ${InspectionTool.Inspectcode.dysplayName}.")
                ),
                arrayOf(
                        InspectionTool.Inspectcode,
                        ParametersServiceStub(mapOf(RUNNER_SETTING_CLT_PLATFORM to IspectionToolPlatform.X86.id)),
                        OSType.WINDOWS,
                        null,
                        RunBuildException("Cannot find ${InspectionTool.Inspectcode.dysplayName}.")
                )
        )
    }

    @Test(dataProvider = "resolveCases")
    fun shouldResolve(
            tool: InspectionTool,
            parametersService: ParametersService,
            os: OSType,
            expectedProcess: InspectionProcess?,
            expectedException: RunBuildException?) {
        // Given
        var actualProcess: InspectionProcess? = null
        val resolver = createInstance(parametersService)
        every { _virtualContext.targetOSType } returns os

        // When
        try {
            actualProcess = resolver.resolve(tool)
        } catch (actualException: RunBuildException) {
            Assert.assertEquals(actualException.message, expectedException?.message)
        }

        // Then
        if (expectedException == null) {
            Assert.assertEquals(actualProcess, expectedProcess)
        }
    }

    private fun createInstance(parametersService: ParametersService) =
            ProcessResolverImpl(parametersService, _virtualContext)
}