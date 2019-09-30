package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.rx.Disposable
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.InputStreamReader

class ResponseFileArgumentsProviderTest {
    @MockK private lateinit var _pathService: PathsService
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _msBuildParameterConverter: MSBuildParameterConverter
    @MockK private lateinit var _sharedCompilation: SharedCompilation
    @MockK private lateinit var _virtualContext: VirtualContext

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { arg<String>(0)}
    }

    @Test
    fun shouldProvideArguments() {
        // Given
        val rspFileName = "rspFile"
        val rspFile = File(rspFileName)
        val fileSystemService = VirtualFileSystemService()
        val argsProvider1 = ArgumentsProviderStub(sequenceOf(CommandLineArgument("arg1"), CommandLineArgument("arg2")))
        val argsProvider2 = ArgumentsProviderStub(emptySequence())
        val argsProvider3 = ArgumentsProviderStub(sequenceOf(CommandLineArgument("arg3")))
        val buildParameter1 = MSBuildParameter("param1", "val1")
        val parametersProvider1 = mockk<MSBuildParametersProvider>()
        val buildParameter2 = MSBuildParameter("param2", "val2")
        val parametersProvider2 = mockk<MSBuildParametersProvider>()
        val argumentsProvider = createInstance(fileSystemService, listOf(argsProvider1, argsProvider2, argsProvider3), listOf(parametersProvider1, parametersProvider2))
        val context = DotnetBuildContext(Path(File("wd")), mockk<DotnetCommand>(), Version(1, 2), Verbosity.Detailed)

        every { parametersProvider1.getParameters(context) } returns sequenceOf(buildParameter1)
        every { _msBuildParameterConverter.convert(buildParameter1) } returns "par1"

        every { parametersProvider2.getParameters(context) } returns sequenceOf(buildParameter2)
        every { _msBuildParameterConverter.convert(buildParameter2) } returns "par2"

        every { _pathService.getTempFileName(ResponseFileArgumentsProvider.ResponseFileExtension) } returns File(rspFileName)
        val blockToken = mockk<Disposable> {
            every { dispose() } returns Unit
        }

        every { _loggerService.writeBlock(ResponseFileArgumentsProvider.BlockName) } returns blockToken
        every { _loggerService.writeStandardOutput(any(), Color.Details) } returns Unit
        every { _sharedCompilation.requireSuppressing(Version(1, 2)) } returns true

        // When
        val actualArguments = argumentsProvider.getArguments(context).toList()

        // Then
        verify { blockToken.dispose() }
        Assert.assertEquals(actualArguments, listOf(CommandLineArgument("@${rspFile.path}", CommandLineArgumentType.Infrastructural)))
        fileSystemService.read(rspFile) {
            InputStreamReader(it).use {
                Assert.assertEquals(it.readLines(), listOf("arg1", "arg2", "arg3", "par1", "par2"))
            }
        }
    }

    private fun createInstance(
            fileSystemService: FileSystemService,
            argumentsProviders: List<ArgumentsProvider>,
            parametersProvider: List<MSBuildParametersProvider>): ArgumentsProvider {
        return ResponseFileArgumentsProvider(
                _pathService,
                ArgumentsServiceStub(),
                fileSystemService,
                _loggerService,
                _msBuildParameterConverter,
                argumentsProviders,
                parametersProvider,
                _virtualContext)
    }
}