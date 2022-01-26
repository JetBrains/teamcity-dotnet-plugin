/*
 * Copyright 2000-2022 JetBrains s.r.o.
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

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class LoggerParametersTest {
    @MockK private lateinit var _customArgumentsProvider: ArgumentsProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

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
        val oggerParametersImpl = createInstance(parameters)

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
        val oggerParametersImpl = createInstance(parameters)

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
        val loggerParametersImpl = createInstance(parameters)

        // When
        val actualParameters = loggerParametersImpl.msBuildParameters

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }

    @DataProvider
    fun additionalLoggerParameters(): Array<Array<out Any>> {
        return arrayOf(
                arrayOf(listOf("/clp:abc", "-ConsoleLoggerParameters:Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("/clp:abc;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("/Clp:abc;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("-CLP:abc;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("/consoleloggerparameters:abc;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("/ConsoleLoggerParameters:abc;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("-consoleloggerparameters:abc;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf(" /clp:abc;Xyz  "), listOf("abc", "Xyz")),
                arrayOf(listOf("/clp:abc=24"), listOf("abc=24")),
                arrayOf(listOf("/clp:abc;;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("/clp:abc;  ;Xyz"), listOf("abc", "Xyz")),
                arrayOf(listOf("/clp:"), emptyList()),
                arrayOf(listOf("/clp:"), emptyList()),
                arrayOf(listOf("clp:abc"), emptyList()),
                arrayOf(listOf("/lcp:abc=24"), emptyList()),
                arrayOf(listOf(" "), emptyList()),
                arrayOf(listOf(""), emptyList())
        )
    }

    @Test(dataProvider = "additionalLoggerParameters")
    fun shouldProvideAdditionalLoggerParameters(
            args: List<String>,
            expectedParameters: List<String>) {
        // Given
        val loggerParametersImpl = createInstance(emptyMap())
        val context = DotnetBuildContext(ToolPath(Path("a"), Path("b")), mockk<DotnetCommand>())

        // When
        every { _customArgumentsProvider.getArguments(context) } returns args.map { CommandLineArgument(it, CommandLineArgumentType.Secondary) }.asSequence()
        val actualParameters = loggerParametersImpl.getAdditionalLoggerParameters(context).toList()

        // Then
        Assert.assertEquals(actualParameters, expectedParameters)
    }

    private fun createInstance(parameters: Map<String, String>)
            = LoggerParametersImpl(ParametersServiceStub(parameters), _customArgumentsProvider)
}