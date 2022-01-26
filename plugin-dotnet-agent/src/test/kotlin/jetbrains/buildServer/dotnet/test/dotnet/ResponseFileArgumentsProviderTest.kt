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

import io.mockk.*
import io.mockk.impl.annotations.MockK
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
    @MockK private lateinit var _responseFileFactory: ResponseFileFactory
    @MockK private lateinit var _argumentsService: ArgumentsService

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @Test
    fun shouldProvideArguments() {
        // Given
        val rspFileName = "rspFile"
        val argsProvider1 = ArgumentsProviderStub(sequenceOf(CommandLineArgument("arg1"), CommandLineArgument("arg2")))
        val argsProvider2 = ArgumentsProviderStub(emptySequence())
        val argsProvider3 = ArgumentsProviderStub(sequenceOf(CommandLineArgument("arg3")))
        val argumentsProvider = createInstance(listOf(argsProvider1, argsProvider2, argsProvider3))
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version(1, 2), Verbosity.Detailed)

        every {
            _responseFileFactory.createResponeFile(
                "",
                any(),
                any(),
                Verbosity.Detailed) } returns Path(rspFileName)

        // When
        val actualArguments = argumentsProvider.getArguments(context).toList()

        // Then
        verify {
            _responseFileFactory.createResponeFile(
                "",
                match { it.toList().equals(listOf(CommandLineArgument("arg1"), CommandLineArgument("arg2"), CommandLineArgument("arg3"))) },
                emptySequence<MSBuildParameter>(),
                Verbosity.Detailed)
        }

        Assert.assertEquals(actualArguments, listOf(CommandLineArgument("@${rspFileName}", CommandLineArgumentType.Infrastructural)))
    }

    private fun createInstance(argumentsProviders: List<ArgumentsProvider>) =
        ResponseFileArgumentsProvider(
                _responseFileFactory,
                argumentsProviders)
}