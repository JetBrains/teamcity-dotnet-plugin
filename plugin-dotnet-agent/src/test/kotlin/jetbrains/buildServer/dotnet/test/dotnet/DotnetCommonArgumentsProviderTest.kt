package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetCommonArgumentsProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _msBuildParametersProvider: MSBuildParametersProvider
    private lateinit var _msBuildParameterConverter: MSBuildParameterConverter

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _msBuildParametersProvider = _ctx.mock(MSBuildParametersProvider::class.java)
        _msBuildParameterConverter = _ctx.mock(MSBuildParameterConverter::class.java)
    }

    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(emptyMap<String, String>(), listOf("customArg", "rspArg")),
                arrayOf(mapOf(DotnetConstants.PARAM_RSP to "true"), listOf("customArg", "rspArg")),
                arrayOf(mapOf(DotnetConstants.PARAM_RSP to "false"), listOf("customArg", "l:/logger", "/p:param=value", "/nodeReuse:false")))
    }

    @Test(dataProvider = "testData")
    fun shouldGetArguments(parameters: Map<String, String>, expectedArguments: List<String>) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), _ctx.mock(DotnetCommand::class.java))
        val msBuildParameter = MSBuildParameter("Param1", "Value1")
        _ctx.checking(object : Expectations() {
            init {
                allowing<MSBuildParametersProvider>(_msBuildParametersProvider).getParameters(context)
                will(returnValue(sequenceOf(msBuildParameter)))

                allowing<MSBuildParameterConverter>(_msBuildParameterConverter).convert(msBuildParameter)
                will(returnValue("/p:param=value"))
            }
        })

        val argumentsProvider = DotnetCommonArgumentsProviderImpl(
                ParametersServiceStub(parameters),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("rspArg"))),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("customArg"))),
                _msBuildParametersProvider,
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("l:/logger"))),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("/nodeReuse:false"))),
                _msBuildParameterConverter)

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}