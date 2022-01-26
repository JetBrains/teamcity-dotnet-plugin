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
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetCommonArgumentsProviderTest {
    @DataProvider
    fun testData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(false, listOf("rspArg", "customArg")),
                arrayOf(true, listOf("l:/logger", "/nodeReuse:false", "customArg")))
    }

    @Test(dataProvider = "testData")
    fun shouldGetArguments(avoidUsingRspFiles: Boolean, expectedArguments: List<String>) {
        // Given
        val context = DotnetBuildContext(ToolPath(Path("wd")), mockk<DotnetCommand>())
        val argumentsProvider = DotnetCommonArgumentsProviderImpl(
                avoidUsingRspFiles,
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("rspArg"))),
                ArgumentsProviderStub(sequenceOf(CommandLineArgument("customArg"))),
                listOf(
                        ArgumentsProviderStub(sequenceOf(CommandLineArgument("l:/logger"))),
                        ArgumentsProviderStub(sequenceOf(CommandLineArgument("/nodeReuse:false")))))

        // When
        val actualArguments = argumentsProvider.getArguments(context).map { it.value }.toList()

        // Then
        Assert.assertEquals(actualArguments, expectedArguments)
    }
}