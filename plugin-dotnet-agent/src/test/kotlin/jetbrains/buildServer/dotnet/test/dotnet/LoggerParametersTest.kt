/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        val oggerParametersImpl = LoggerParametersImpl(ParametersServiceStub(parameters))

        // When
        val actualParamVerbosity = oggerParametersImpl.paramVerbosity
        val actualMSBuildLoggerVerbosity = oggerParametersImpl.msBuildLoggerVerbosity

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
        val oggerParametersImpl = LoggerParametersImpl(ParametersServiceStub(parameters))

        // When
        val actualVerbosity = oggerParametersImpl.vsTestVerbosity

        // Then
        Assert.assertEquals(actualVerbosity, expectedVerbosity)
    }

    @DataProvider
    fun msBuildParameters(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_MSBUILD_LOGGER_PARAMS, "Abc;WXY")), "Abc;WXY"),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_MSBUILD_LOGGER_PARAMS, "")), ""),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_MSBUILD_LOGGER_PARAMS, " ")), " "),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_MSBUILD_LOGGER_PARAMS, null)), LoggerParametersImpl.defaultMsBuildLoggerParams))
    }

    @Test(dataProvider = "msBuildParameters")
    fun shouldProvideMSBuildParameters(
            parameters: Map<String, String>,
            expectedParameters: String) {
        // Given
        val loggerParametersImpl = LoggerParametersImpl(ParametersServiceStub(parameters))

        // When
        val actualParameters = loggerParametersImpl.msBuildParameters

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }
}