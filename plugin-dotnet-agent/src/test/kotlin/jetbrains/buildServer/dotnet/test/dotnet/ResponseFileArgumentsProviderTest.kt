package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.ArgumentsServiceStub
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.InputStreamReader

class ResponseFileArgumentsProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _pathService: PathsService
    private lateinit var _loggerService: LoggerService
    private lateinit var _msBuildParameterConverter: MSBuildParameterConverter

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _pathService = _ctx.mock(PathsService::class.java)
        _loggerService = _ctx.mock(LoggerService::class.java)
        _msBuildParameterConverter = _ctx.mock(MSBuildParameterConverter::class.java)
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
        val argsProvider3 = DotnetCommonArgumentsProviderStub(sequenceOf(CommandLineArgument("arg3")))
        val buildParameter1 = MSBuildParameter("param1", "val1")
        val parametersProvider1 = _ctx.mock(MSBuildParametersProvider::class.java, "parametersProvider1")
        val buildParameter2 = MSBuildParameter("param2", "val2")
        val parametersProvider2 = _ctx.mock(MSBuildParametersProvider::class.java, "parametersProvider2")
        val argumentsProvider = createInstance(fileSystemService, listOf(argsProvider1, argsProvider2, argsProvider3), listOf(parametersProvider1, parametersProvider2))
        val context = DotnetBuildContext(_ctx.mock(DotnetCommand::class.java), Verbosity.Detailed)

        // When
        _ctx.checking(object : Expectations() {
            init {
                oneOf<PathsService>(_pathService).getPath(PathType.AgentTemp)
                will(returnValue(tempDirectory))

                oneOf<MSBuildParametersProvider>(parametersProvider1).getParameters(context)
                will(returnValue(sequenceOf(buildParameter1)))

                oneOf<Converter<MSBuildParameter, String>>(_msBuildParameterConverter).convert(buildParameter1)
                will(returnValue("par1"))

                oneOf<MSBuildParametersProvider>(parametersProvider2).getParameters(context)
                will(returnValue(sequenceOf(buildParameter2)))

                oneOf<Converter<MSBuildParameter, String>>(_msBuildParameterConverter).convert(buildParameter2)
                will(returnValue("par2"))

                oneOf<PathsService>(_pathService).uniqueName
                will(returnValue(rspFileName))

                oneOf<LoggerService>(_loggerService).writeBlock(ResponseFileArgumentsProvider.BlockName)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("arg1", Color.Details)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("arg2", Color.Details)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("arg3", Color.Details)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("par1", Color.Details)
                oneOf<LoggerService>(_loggerService).writeStandardOutput("par2", Color.Details)
            }
        })

        val actualArguments = argumentsProvider.getArguments(context).toList()

        // Then
        _ctx.assertIsSatisfied()
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
                _pathService,
                ArgumentsServiceStub(),
                fileSystemService,
                _loggerService,
                _msBuildParameterConverter,
                argumentsProviders,
                parametersProvider)
    }
}