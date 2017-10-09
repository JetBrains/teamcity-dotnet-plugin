package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotcover.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.io.InputStreamReader

@Suppress("UNCHECKED_CAST")
class ResponseFileArgumentsProviderTest {
    private var _ctx: Mockery? = null
    private var _pathService: PathsService? = null
    private var _parametersService: ParametersService? = null
    private var _loggerService: LoggerService? = null
    private var _msBuildParameterConverter: MSBuildParameterConverter? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx!!.mock(PathsService::class.java)
        _parametersService = _ctx!!.mock(ParametersService::class.java)
        _loggerService = _ctx!!.mock(LoggerService::class.java)
        _msBuildParameterConverter = _ctx!!.mock(MSBuildParameterConverter::class.java)
    }

    @Test
    fun shouldProvideArguments() {
        // Given
        val tempDirectory = File("temp")
        val rspFileName = "rspFile"
        val rspFile = File(tempDirectory, rspFileName + ResponseFileArgumentsProvider.ResponseFileExtension).absoluteFile
        val fileSystemService = VirtualFileSystemService()
        val argsProvider1 = DotnetCommonArgumentsProviderStub(sequenceOf(CommandLineArgument("arg1"), CommandLineArgument("arg2")))
        val argsProvider2 = DotnetCommonArgumentsProviderStub(emptySequence())
        val argsProvider3 = DotnetCommonArgumentsProviderStub(sequenceOf( CommandLineArgument("arg3")))
        val buildParameter1 = MSBuildParameter("param1", "val1")
        val parametersProvider1 = _ctx!!.mock(MSBuildParametersProvider::class.java, "parametersProvider1")
        val buildParameter2 = MSBuildParameter("param2", "val2")
        val parametersProvider2 = _ctx!!.mock(MSBuildParametersProvider::class.java, "parametersProvider2")
        val argumentsProvider = createInstance(fileSystemService, listOf(argsProvider1, argsProvider2, argsProvider3), listOf(parametersProvider1, parametersProvider2))

        // When
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ParametersService>(_parametersService).tryGetParameter(ParameterType.Runner, DotnetConstants.PARAM_VERBOSITY)
                will(returnValue(Verbosity.Detailed.id))

                oneOf<PathsService>(_pathService).getPath(PathType.BuildTemp)
                will(returnValue(tempDirectory))

                oneOf<MSBuildParametersProvider>(parametersProvider1).parameters
                will(returnValue(sequenceOf(buildParameter1)))

                oneOf<Converter<MSBuildParameter, String>>(_msBuildParameterConverter).convert(buildParameter1)
                will(returnValue("par1"))

                oneOf<MSBuildParametersProvider>(parametersProvider2).parameters
                will(returnValue(sequenceOf(buildParameter2)))

                oneOf<Converter<MSBuildParameter, String>>(_msBuildParameterConverter).convert(buildParameter2)
                will(returnValue("par2"))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(rspFileName))

                oneOf<LoggerService>(_loggerService).onBlock(ResponseFileArgumentsProvider.BlockName)
                oneOf<LoggerService>(_loggerService).onStandardOutput("arg1", Color.Details)
                oneOf<LoggerService>(_loggerService).onStandardOutput("arg2", Color.Details)
                oneOf<LoggerService>(_loggerService).onStandardOutput("arg3", Color.Details)
                oneOf<LoggerService>(_loggerService).onStandardOutput("par1", Color.Details)
                oneOf<LoggerService>(_loggerService).onStandardOutput("par2", Color.Details)
            }
        })

        val actualArguments = argumentsProvider.arguments.toList()

        // Then
        _ctx!!.assertIsSatisfied()
        Assert.assertEquals(actualArguments, listOf(CommandLineArgument("@${rspFile.path}")))
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
                _pathService!!,
                ArgumentsServiceStub(),
                _parametersService!!,
                fileSystemService,
                _loggerService!!,
                _msBuildParameterConverter!!,
                argumentsProviders,
                parametersProvider)
    }
}