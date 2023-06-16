/*
 * Copyright 2000-2023 JetBrains s.r.o.
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

package jetbrains.buildServer.dotnet.test.dotnet.commands.msbuild

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.VirtualContext
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameter
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameterType
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildVSTestLoggerParametersProvider
import jetbrains.buildServer.dotnet.commands.test.TestReportingParameters
import jetbrains.buildServer.dotnet.logging.LoggerParameters
import jetbrains.buildServer.dotnet.test.dotnet.logging.LoggerResolverStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File
import java.util.*

class MSBuildVSTestLoggerParametersProviderTest {
    @MockK
    private lateinit var _pathsService: PathsService

    @MockK
    private lateinit var _parametersService: ParametersService

    @MockK
    private lateinit var _testReportingParameters: TestReportingParameters

    @MockK
    private lateinit var _msBuildVSTestLoggerParameters: LoggerParameters

    @MockK
    private lateinit var _virtualContext: VirtualContext

    @MockK
    private lateinit var _customArgumentsProvider: CustomArgumentsProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { "v_" + arg<String>(0) }
    }

    @DataProvider
    fun testLoggerArgumentsData(): Array<Array<Any?>> {
        return arrayOf(
            // Success scenario
            arrayOf(
                File("loggerPath", "vstestlogger.dll") as File?,
                EnumSet.of(TestReportingMode.On),
                listOf(
                    MSBuildParameter("VSTestLogger", "logger://teamcity", MSBuildParameterType.Predefined),
                    MSBuildParameter(
                        "VSTestTestAdapterPath",
                        "v_" + File("checkoutDir").canonicalPath,
                        MSBuildParameterType.Predefined
                    ),
                    MSBuildParameter(
                        "VSTestVerbosity",
                        Verbosity.Detailed.id.lowercase(Locale.getDefault()),
                        MSBuildParameterType.Predefined
                    )
                )
            ),

            // Supports mult VSTestTestAdapterPath (.NET Core SDK 2.1.102)
            arrayOf(
                File("loggerPath", "vstestlogger.dll") as File?,
                EnumSet.of(TestReportingMode.MultiAdapterPath),
                listOf(
                    MSBuildParameter("VSTestLogger", "logger://teamcity", MSBuildParameterType.Predefined),
                    MSBuildParameter(
                        "VSTestTestAdapterPath",
                        "v_${File("loggerPath").canonicalPath};.",
                        MSBuildParameterType.Predefined
                    ),
                    MSBuildParameter(
                        "VSTestVerbosity",
                        Verbosity.Detailed.id.lowercase(Locale.getDefault()),
                        MSBuildParameterType.Predefined
                    )
                )
            ),

            // Supports mult VSTestTestAdapterPath (.NET SDK 5.0.103)
            arrayOf(
                File("loggerPath", "vstestlogger.dll") as File?,
                EnumSet.of(TestReportingMode.MultiAdapterPath_5_0_103),
                listOf(
                    MSBuildParameter("VSTestLogger", "logger://teamcity", MSBuildParameterType.Predefined),
                    MSBuildParameter(
                        "VSTestTestAdapterPath",
                        ".;v_${File("loggerPath").canonicalPath}",
                        MSBuildParameterType.Predefined
                    ),
                    MSBuildParameter(
                        "VSTestVerbosity",
                        Verbosity.Detailed.id.lowercase(Locale.getDefault()),
                        MSBuildParameterType.Predefined
                    )
                )
            ),

            // Reporting is off
            arrayOf(
                File("loggerPath", "vstestlogger.dll") as File?,
                EnumSet.of(TestReportingMode.Off),
                emptyList<MSBuildParameter>()
            )
        )
    }

    @Test(dataProvider = "testLoggerArgumentsData")
    fun shouldGetArguments(
        loggerFile: File,
        testReportingMode: EnumSet<TestReportingMode>,
        expectedParameters: List<MSBuildParameter>
    ) {
        // Given
        val checkoutDirectory = File("checkoutDir")
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val argumentsProvider = MSBuildVSTestLoggerParametersProvider(
            _pathsService,
            _parametersService,
            LoggerResolverStub(File("msbuildlogger"), loggerFile),
            _testReportingParameters,
            _msBuildVSTestLoggerParameters,
            _virtualContext,
            _customArgumentsProvider
        )
        every { _testReportingParameters.getMode(context) } returns testReportingMode
        every { _pathsService.getPath(PathType.Checkout) } returns checkoutDirectory
        every { _msBuildVSTestLoggerParameters.vsTestVerbosity } returns Verbosity.Detailed
        every {
            _parametersService.tryGetParameter(
                any(),
                DotnetConstants.PARAM_MSBUILD_DISABLE_CUSTOM_VSTEST_LOGGERS
            )
        } returns "true" // feature is off

        // When
        val actualParameters = argumentsProvider.getParameters(context).toList()

        // Then
        if (expectedParameters.any()) {
            verify { _virtualContext.resolvePath(any()) }
        }

        Assert.assertEquals(actualParameters, expectedParameters)
    }

    @DataProvider
    fun testCustomLoggersData(): Array<Array<Any?>> {
        return arrayOf(
            // Enabled, no custom arguments
            arrayOf(
                emptySequence<CommandLineArgument>(),
                "false",
                MSBuildParameter("VSTestLogger", "teamcity", MSBuildParameterType.Unknown),
            ),

            // Enabled, no custom loggers
            arrayOf(
                sequenceOf(CommandLineArgument("--nologo")),
                "false",
                MSBuildParameter("VSTestLogger", "teamcity", MSBuildParameterType.Unknown),
            ),

            // Enabled, one custom logger
            arrayOf(
                sequenceOf(CommandLineArgument("--logger"), CommandLineArgument("trx")),
                "false",
                MSBuildParameter("VSTestLogger", "teamcity;trx", MSBuildParameterType.Unknown),
            ),

            // Enabled, multiple sophisticated loggers mixed with other arguments
            arrayOf(
                sequenceOf(
                    CommandLineArgument("--logger"), CommandLineArgument("trx;LogFileName=checkoutDir/log.trx"),
                    CommandLineArgument("--logger"), // without value - should be ignored
                    CommandLineArgument("--nologo"),
                    CommandLineArgument("--no-restore"),
                    CommandLineArgument("-l"), CommandLineArgument("html;LogFileName=~/log.html"),
                    CommandLineArgument("--output"), CommandLineArgument("checkoutDir"),
                ),
                "false",
                MSBuildParameter(
                    "VSTestLogger",
                    "teamcity;trx%3BLogFileName%3DcheckoutDir/log.trx;html%3BLogFileName%3D%7E/log.html",
                    MSBuildParameterType.Unknown
                ),
            ),

            // Enabled, broken logger (no value)
            arrayOf(
                sequenceOf(
                    CommandLineArgument("--logger"),
                    CommandLineArgument("--nologo"),
                    CommandLineArgument("--no-restore"),
                ),
                "false",
                MSBuildParameter("VSTestLogger", "teamcity", MSBuildParameterType.Unknown),
            ),

            // Disabled
            arrayOf(
                sequenceOf(
                    CommandLineArgument("--logger"), CommandLineArgument("trx"),
                    CommandLineArgument("-l"), CommandLineArgument("html"),
                ),
                "true",
                MSBuildParameter("VSTestLogger", "logger://teamcity", MSBuildParameterType.Predefined),
            )
        )
    }

    @Test(dataProvider = "testCustomLoggersData")
    fun `should add custom loggers when feature is on`(
        customArguments: Sequence<CommandLineArgument>,
        isCustomLoggersDisabled: String,
        expectedParameter: MSBuildParameter
    ) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val argumentsProvider = MSBuildVSTestLoggerParametersProvider(
            _pathsService,
            _parametersService,
            LoggerResolverStub(File("msbuildlogger"), File("loggerPath", "vstestlogger.dll")),
            _testReportingParameters,
            _msBuildVSTestLoggerParameters,
            _virtualContext,
            _customArgumentsProvider
        )
        every { _testReportingParameters.getMode(context) } returns EnumSet.of(TestReportingMode.On)
        every { _pathsService.getPath(PathType.Checkout) } returns File("checkoutDir")
        every {
            _parametersService.tryGetParameter(
                any(),
                DotnetConstants.PARAM_MSBUILD_DISABLE_CUSTOM_VSTEST_LOGGERS
            )
        } returns isCustomLoggersDisabled
        every { _msBuildVSTestLoggerParameters.vsTestVerbosity } returns Verbosity.Detailed
        every { _customArgumentsProvider.getArguments(any()) } returns customArguments

        // When
        val actualParameters = argumentsProvider.getParameters(context).toList()

        // Then
        Assert.assertEquals(actualParameters.size, 3)
        Assert.assertTrue(expectedParameter.let { actualParameters.contains(it) })
    }
}