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

import io.mockk.mockk
import jetbrains.buildServer.agent.CommandLineArgument
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.dotnet.*
import org.testng.Assert
import org.testng.annotations.Test

class SharedCompilationArgumentsProviderTest {
    @Test
    fun shouldProvideNodeReuseArgumentsWhenSharedCompilationRequiresSuppressing() {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version(2, 1, 106), Verbosity.Detailed)

        // When
        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        Assert.assertEquals(actualArguments, listOf(SharedCompilationArgumentsProvider.nodeReuseArgument))
    }

    @Test
    fun shouldProvideNodeReuseArgumentsWhenSharedCompilationDoesNotRequireSuppressing() {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>(), Version.LastVersionWithoutSharedCompilation, Verbosity.Detailed)

        // When
        val actualArguments = createInstance().getArguments(context).toList()

        // Then
        Assert.assertEquals(actualArguments, emptyList<CommandLineArgument>())
    }


    private fun createInstance(): ArgumentsProvider {
        return SharedCompilationArgumentsProvider()
    }
}