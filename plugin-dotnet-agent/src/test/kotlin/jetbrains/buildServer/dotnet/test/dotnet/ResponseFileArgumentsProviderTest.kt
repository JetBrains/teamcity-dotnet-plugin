/*
 * Copyright 2000-2021 JetBrains s.r.o.
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

import io.mockk.*
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.FileSystemService
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.Color
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.agent.runner.PathsService
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.VirtualFileSystemService
import jetbrains.buildServer.rx.Disposable
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.Test
import java.io.File
import java.io.InputStreamReader

class ResponseFileArgumentsProviderTest {
    @MockK private lateinit var _pathService: PathsService
    @MockK private lateinit var _loggerService: LoggerService
    @MockK private lateinit var _msBuildParameterConverter: MSBuildParameterConverter
    @MockK private lateinit var _virtualContext: VirtualContext
    @MockK private lateinit var _argumentsService: ArgumentsService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
        every { _virtualContext.resolvePath(any()) } answers { arg<String>(0)}
        every { _argumentsService.normalize(any()) } answers { "\"${arg<String>(0)}\""}
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
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version(1, 2), Verbosity.Detailed)
        val buildParameterInvalid = MSBuildParameter("#$%", "*((val1")

        every { parametersProvider1.getParameters(context) } returns sequenceOf(buildParameter1, buildParameterInvalid)
        every { parametersProvider2.getParameters(context) } returns sequenceOf(buildParameter2)
        every { _msBuildParameterConverter.convert(match { it.toList().equals(listOf(buildParameter1, buildParameterInvalid, buildParameter2)) }, false) } returns sequenceOf("par1", "par2")

        every { _pathService.getTempFileName(ResponseFileArgumentsProvider.ResponseFileExtension) } returns File(rspFileName)
        val blockToken = mockk<Disposable> {
            every { dispose() } returns Unit
        }

        every { _loggerService.writeBlock(ResponseFileArgumentsProvider.BlockName) } returns blockToken
        every { _loggerService.writeStandardOutput(any(), Color.Details) } returns Unit

        // When
        val actualArguments = argumentsProvider.getArguments(context).toList()

        // Then
        verify { blockToken.dispose() }
        Assert.assertEquals(actualArguments, listOf(CommandLineArgument("@${rspFile.path}", CommandLineArgumentType.Infrastructural)))
        fileSystemService.read(rspFile) {
            InputStreamReader(it).use {
                Assert.assertEquals(it.readLines(), listOf("\"arg1\"", "\"arg2\"", "\"arg3\"", "par1", "par2"))
            }
        }
    }

    private fun createInstance(
            fileSystemService: FileSystemService,
            argumentsProviders: List<ArgumentsProvider>,
            parametersProvider: List<MSBuildParametersProvider>): ArgumentsProvider {
        return ResponseFileArgumentsProvider(
                _pathService,
                _argumentsService,
                fileSystemService,
                _loggerService,
                _msBuildParameterConverter,
                argumentsProviders,
                parametersProvider,
                _virtualContext)
    }
}