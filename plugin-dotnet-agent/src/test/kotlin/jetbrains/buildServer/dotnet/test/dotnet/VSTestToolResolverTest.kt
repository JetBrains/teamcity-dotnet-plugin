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

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolPath
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestToolResolverTest {

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest12Windows.id, "teamcity.dotnet.vstest.12.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest14Windows.id, "teamcity.dotnet.vstest.14.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id, "teamcity.dotnet.vstest.15.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTestCrossPlatform.id), File("dotnet"), null),
                arrayOf(mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id), File(""), Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: teamcity.dotnet.vstest.15.0")))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideExecutableFile(
            parameters: Map<String, String>,
            expectedExecutableFile: File,
            exceptionPattern: Regex?) {
        // Given
        val instance = createInstance(parameters, File("dotnet"))

        // When
        var actualExecutable: ToolPath? = null
        try {
            actualExecutable = instance.executable
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        } catch (ex: RunBuildException) {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true)
        }


        // Then
        if (exceptionPattern == null) {
            Assert.assertEquals(actualExecutable, ToolPath(Path(expectedExecutableFile.path)))
        }
    }

    private fun createInstance(parameters: Map<String, String>, executableFile: File): ToolResolver {
        return VSTestToolResolver(ParametersServiceStub(parameters), DotnetToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path(executableFile.path)),true))
    }
}