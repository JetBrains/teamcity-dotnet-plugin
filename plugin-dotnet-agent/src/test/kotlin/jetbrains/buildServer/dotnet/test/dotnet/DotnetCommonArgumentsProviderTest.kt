package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommonArgumentsProviderTest {
    @DataProvider
    fun argumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VERBOSITY to Verbosity.Normal.id,
                            DotnetConstants.PARAM_RSP to "true"),
                        listOf("--verbosity", Verbosity.Normal.id)),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VERBOSITY to Verbosity.Normal.id),
                        listOf("--verbosity", Verbosity.Normal.id)),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_RSP to "false"),
                        listOf("l:/logger", "/p:param=value")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_RSP to "FaLse"),
                        listOf("l:/logger", "/p:param=value")),
                arrayOf(emptyMap<String, String>(),
                        emptyList<String>()),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_VERBOSITY to Verbosity.Detailed.id),
                        listOf("--verbosity", Verbosity.Detailed.id)))
    }

    @Test(dataProvider = "argumentsData")
    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val ctx = Mockery()
        val msBuildParametersProvider = ctx.mock(MSBuildParametersProvider::class.java)
        val msBuildParameterConverter = ctx.mock(MSBuildParameterConverter::class.java)
        val msBuildParameter = MSBuildParameter("Param1", "Value1")
        ctx!!.checking(object : Expectations() {
            init {
                allowing<MSBuildParametersProvider>(msBuildParametersProvider).parameters
                will(returnValue(sequenceOf(msBuildParameter)))

                allowing<MSBuildParameterConverter>(msBuildParameterConverter).convert(msBuildParameter)
                will(returnValue("/p:param=value"))
            }
        })

        val argumentsProvider = DotnetCommonArgumentsProviderImpl(
                ParametersServiceStub(parameters),
                DotnetCommonArgumentsProviderStub(),
                DotnetCommonArgumentsProviderStub(),
                msBuildParametersProvider,
                DotnetCommonArgumentsProviderStub(sequenceOf(CommandLineArgument("l:/logger"))),
                msBuildParameterConverter)

        // When
        val actualArguments = argumentsProvider.arguments.map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}