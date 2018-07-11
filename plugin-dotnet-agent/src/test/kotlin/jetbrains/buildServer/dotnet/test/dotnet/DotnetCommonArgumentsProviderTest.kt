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


    fun shouldGetArguments(
            parameters: Map<String, String>,
            expectedArguments: List<String>) {
        // Given
        val ctx = Mockery()
        val context = DotnetBuildContext(ctx.mock(DotnetCommand::class.java))
        val msBuildParametersProvider = ctx.mock(MSBuildParametersProvider::class.java)
        val msBuildParameterConverter = ctx.mock(MSBuildParameterConverter::class.java)
        val msBuildParameter = MSBuildParameter("Param1", "Value1")
        ctx.checking(object : Expectations() {
            init {
                allowing<MSBuildParametersProvider>(msBuildParametersProvider).getParameters(context)
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
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}