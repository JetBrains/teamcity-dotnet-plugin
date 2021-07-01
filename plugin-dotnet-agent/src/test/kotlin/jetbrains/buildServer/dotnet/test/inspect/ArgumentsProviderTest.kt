package jetbrains.buildServer.dotnet.test.inspect

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.CommandLineArgumentType
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.inspect.ArgumentsProviderImpl
import jetbrains.buildServer.inspect.InspectCodeConstants
import jetbrains.buildServer.inspect.InspectionArguments
import jetbrains.buildServer.inspect.InspectionTool
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class ArgumentsProviderTest {
    @MockK private lateinit var _pathsService: PathsService

    private val _tmp = File("tmp")
    private val _checkout = File("checkout")
    private val _cache = File("defaultCache")

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()

        every { _pathsService.getPath(PathType.AgentTemp) } returns _tmp
        every { _pathsService.getPath(PathType.Checkout) } returns _checkout
        every { _pathsService.getPath(PathType.CachePerCheckout) } returns _cache
    }

    @DataProvider
    fun argsCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1\r\n--Arg  2")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
                        )
                ),

                // Cusrom args separated by \n (https://youtrack.jetbrains.com/issue/TW-72039)
                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1\n--Arg  2")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1\n  \n--Arg  2")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1\r\n  \r\n--Arg  2")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1\r--Arg  2")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg  2", CommandLineArgumentType.Custom))
                        )
                ),

                // Cusrom args with spaces (https://youtrack.jetbrains.com/issue/TW-71722)
                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg  1" + System.lineSeparator() + "--Arg2")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg  1", CommandLineArgumentType.Custom), CommandLineArgument("--Arg2", CommandLineArgumentType.Custom))
                        )
                ),

                // Override cache
                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--caches-home=Cache")),
                        VirtualFileSystemService().addDirectory(File("Cache"), VirtualFileSystemService.Attributes(true)),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                File("Cache"),
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--caches-home=Cache")),
                        VirtualFileSystemService().addDirectory(File("Cache"), VirtualFileSystemService.Attributes(false)),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                File(_checkout, "Cache"),
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                // Override log
                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--logFile=Abc.log")),
                        VirtualFileSystemService().addFile(File("Abc.log"), VirtualFileSystemService.Attributes(true)),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File("Abc.log"),
                                _cache,
                                true,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--logFile=Abc.log")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_checkout, "Abc.log"),
                                _cache,
                                true,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                // Override output
                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--output=Out.xml")),
                        VirtualFileSystemService().addFile(File("Out.xml"), VirtualFileSystemService.Attributes(true)),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File("Out.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--output=Out.xml")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_checkout, "Out.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "-o=Out.xml")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_tmp, "dotnet-tools-inspectcode99.config"),
                                File(_checkout, "Out.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                // Override config path
                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--config=Cfg.xml")),
                        VirtualFileSystemService().addFile(File("Cfg.xml"), VirtualFileSystemService.Attributes(true)),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File("Cfg.xml"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                ),

                arrayOf(
                        ParametersServiceStub(mapOf(InspectCodeConstants.RUNNER_SETTING_CUSTOM_CMD_ARGS to "--Arg1" + System.lineSeparator() + "--config=Cfg.xml")),
                        VirtualFileSystemService(),
                        InspectionTool.Inspectcode,
                        InspectionArguments(
                                File(_checkout, "Cfg.xml"),
                                File(_tmp, "inspectcode-report99.xml"),
                                File(_tmp, "dotnet-tools-inspectcode99.log"),
                                _cache,
                                false,
                                listOf(CommandLineArgument("--Arg1", CommandLineArgumentType.Custom))
                        )
                )
        )
    }

    @Test(dataProvider = "argsCases")
    fun shouldResolve(
            parametersService: ParametersService,
            fileSystemService: FileSystemService,
            tool: InspectionTool,
            expectedArgs: InspectionArguments) {
        // Given
        val provider = createInstance(parametersService, fileSystemService)

        // When
        val actualArgs = provider.getArguments(tool)

        // Then
        Assert.assertEquals(actualArgs, expectedArgs)
    }

    private fun createInstance(parametersService: ParametersService, fileSystemService: FileSystemService) =
            ArgumentsProviderImpl(
                    parametersService,
                    _pathsService,
                    fileSystemService)
}