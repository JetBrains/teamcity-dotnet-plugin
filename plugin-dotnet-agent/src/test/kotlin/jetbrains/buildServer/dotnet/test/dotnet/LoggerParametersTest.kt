package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.LoggerParametersImpl
import jetbrains.buildServer.dotnet.Verbosity
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class LoggerParametersTest {
    @DataProvider
    fun paramVerbosity(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Quiet.id)), Verbosity.Quiet),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Minimal.id)), Verbosity.Minimal),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Normal.id)), Verbosity.Normal),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Detailed.id)), Verbosity.Detailed),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Diagnostic.id)), Verbosity.Diagnostic),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, "abc")), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, "")), null),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, null)), null),
                arrayOf(emptyMap<String, String>(), null))
    }

    @Test(dataProvider = "paramVerbosity")
    fun shouldProvideParamVerbosity(
            parameters: Map<String, String>,
            expectedVerbosity: Verbosity?) {
        // Given
        val msBuildVSTestLoggerParameters = LoggerParametersImpl(ParametersServiceStub(parameters))

        // When
        val actualParamVerbosity = msBuildVSTestLoggerParameters.paramVerbosity
        val actualMSBuildLoggerVerbosity = msBuildVSTestLoggerParameters.msBuildLoggerVerbosity

        // Then
        Assert.assertEquals(actualParamVerbosity, expectedVerbosity)
        Assert.assertEquals(actualMSBuildLoggerVerbosity, expectedVerbosity)
    }

    @DataProvider
    fun vsTestVerbosity(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Normal.id)), Verbosity.Normal),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Detailed.id)), Verbosity.Detailed),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Diagnostic.id)), Verbosity.Diagnostic),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, "abc")), Verbosity.Normal),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, "")), Verbosity.Normal),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, null)), Verbosity.Normal),
                arrayOf(emptyMap<String, String>(), Verbosity.Normal),

                // https://github.com/xunit/xunit/issues/1706
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Quiet.id)), Verbosity.Normal),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_VERBOSITY, Verbosity.Minimal.id)), Verbosity.Normal))
    }

    @Test(dataProvider = "vsTestVerbosity")
    fun shouldProvideVSTestVerbosity(
            parameters: Map<String, String>,
            expectedVerbosity: Verbosity) {
        // Given
        val msBuildVSTestLoggerParameters = LoggerParametersImpl(ParametersServiceStub(parameters))

        // When
        val actualVerbosity = msBuildVSTestLoggerParameters.vsTestVerbosity

        // Then
        Assert.assertEquals(actualVerbosity, expectedVerbosity)
    }
}