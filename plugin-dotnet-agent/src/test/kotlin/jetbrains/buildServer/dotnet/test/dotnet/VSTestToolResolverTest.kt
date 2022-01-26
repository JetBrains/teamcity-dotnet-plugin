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
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.*
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VSTestToolResolverTest {
    @MockK private lateinit var _virtualContext: VirtualContext
    private val _vstestStateWorkflowComposer: ToolStateWorkflowComposer = mockk<ToolStateWorkflowComposer>()
    private val _dotnetStateWorkflowComposer: ToolStateWorkflowComposer = mockk<ToolStateWorkflowComposer>()

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(false, mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest12Windows.id, "teamcity.dotnet.vstest.12.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null, _vstestStateWorkflowComposer),
                arrayOf(false, mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest14Windows.id, "teamcity.dotnet.vstest.14.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null, _vstestStateWorkflowComposer),
                arrayOf(false, mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id, "teamcity.dotnet.vstest.15.0" to "vstest.console.exe"), File("vstest.console.exe").absoluteFile, null, _vstestStateWorkflowComposer),
                arrayOf(true, mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id, "teamcity.dotnet.vstest.15.0" to "vstest.console.exe"), File("vstest.console.exe"), null, _vstestStateWorkflowComposer),
                arrayOf(false, mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTestCrossPlatform.id), File("dotnet"), null, _dotnetStateWorkflowComposer),
                arrayOf(false, mapOf(DotnetConstants.PARAM_VSTEST_VERSION to Tool.VSTest15Windows.id), File(""), Regex("jetbrains.buildServer.agent.ToolCannotBeFoundException: teamcity.dotnet.vstest.15.0"), null))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideExecutableFile(
            isVirtual: Boolean,
            parameters: Map<String, String>,
            expectedExecutableFile: File,
            exceptionPattern: Regex?,
            expectedToolStateWorkflowComposer: ToolStateWorkflowComposer?) {
        // Given
        val instance = createInstance(parameters, File("dotnet"))

        // When
        every { _virtualContext.isVirtual } returns isVirtual
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
            Assert.assertEquals(instance.toolStateWorkflowComposer, expectedToolStateWorkflowComposer)
            Assert.assertEquals(actualExecutable, ToolPath(Path(expectedExecutableFile.path)))
        }
    }

    private fun createInstance(parameters: Map<String, String>, executableFile: File): ToolResolver {
        return VSTestToolResolver(_virtualContext, ParametersServiceStub(parameters), ToolResolverStub(ToolPlatform.CrossPlatform, ToolPath(Path(executableFile.path)),true, _dotnetStateWorkflowComposer), _vstestStateWorkflowComposer)
    }
}