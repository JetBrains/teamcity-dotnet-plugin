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

package jetbrains.buildServer.dotnet.test.visualStudio

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetConstants.PARAM_VISUAL_STUDIO_VERSION
import jetbrains.buildServer.dotnet.Tool
import jetbrains.buildServer.dotnet.test.agent.runner.ParametersServiceStub
import jetbrains.buildServer.visualStudio.ToolResolver
import jetbrains.buildServer.visualStudio.VisualStudioToolResolver
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class VisualStudioToolResolverTest {
    @DataProvider
    fun testData(): Array<Array<out Any?>> {
        return arrayOf(
                // selected version
                arrayOf(
                        mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to Tool.VisualStudio2015.id,
                                "VS2015_Path" to "vs2015"),
                        File("vs2015", VisualStudioToolResolver.VSToolName), null),

                arrayOf(
                        mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to Tool.VisualStudio2017.id,
                                "VS2017_Path" to "vs2017"),
                        File("vs2017", VisualStudioToolResolver.VSToolName), null),

                // Can't find configuration parameter
                arrayOf(
                        mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to Tool.VisualStudio2015.id),
                        File("vs2015", VisualStudioToolResolver.VSToolName), Regex("Can't find configuration parameter")),

                // Can't parse visual studio version
                arrayOf(
                        mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to "abc"),
                        File("xxx"), Regex("Can't parse visual studio version from \"$PARAM_VISUAL_STUDIO_VERSION\" value \"abc\"")),

                arrayOf(
                        mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to ""),
                        File("xxx"), Regex("Can't parse visual studio version from \"$PARAM_VISUAL_STUDIO_VERSION\" value \"\"")),

                arrayOf(
                        mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to "  "),
                        File("xxx"), Regex("Can't parse visual studio version from \"$PARAM_VISUAL_STUDIO_VERSION\" value \"  \"")),

                // default version
                arrayOf(
                        mapOf(DotnetConstants.PARAM_VISUAL_STUDIO_VERSION to Tool.VisualStudioAny.id,
                                "VS2013_Path" to "vs2013",
                                "VS2019" to "vs2019",
                                "VS2017_Path" to "vs2017",
                                "VS" to "vs",
                                "VS2015_Path" to "vs2015"),
                        File("vs2017", VisualStudioToolResolver.VSToolName), null),

                // Max available
                arrayOf(
                        mapOf("VS2013_Path" to "vs2013",
                                "VS2019" to "vs2019",
                                "VS2017_Path" to "vs2017",
                                "VS" to "vs",
                                "VS2015_Path" to "vs2015"),
                        File("vs2017", VisualStudioToolResolver.VSToolName), null),

                arrayOf(
                        mapOf("VS2019" to "vs2019",
                                "VS2020_Path" to "vs2020",
                                "VSaaa_Path" to "vs",
                                "VS2015_Path" to "vs2015"),
                        File("vs2020", VisualStudioToolResolver.VSToolName), null),

                // Can't find any version of visual studio
                arrayOf(
                        emptyMap<String, String>(),
                        File("xxx"), Regex("Can't find any version of visual studio")))
    }

    @Test(dataProvider = "testData")
    fun shouldProvideExecutableFile(
            parameters: Map<String, String>,
            expectedExecutableFile: File,
            exceptionPattern: Regex?) {
        // Given
        val instance = createInstance(parameters)

        // When
        var actualExecutableFile: File? = null
        try {
            actualExecutableFile = instance.executableFile
            exceptionPattern?.let {
                Assert.fail("Exception should be thrown")
            }
        } catch (ex: RunBuildException) {
            Assert.assertEquals(exceptionPattern!!.containsMatchIn(ex.message!!), true)
        }


        // Then
        if (exceptionPattern == null) {
            Assert.assertEquals(actualExecutableFile, expectedExecutableFile)
        }
    }

    private fun createInstance(parameters: Map<String, String>): ToolResolver {
        return VisualStudioToolResolver(ParametersServiceStub(parameters))
    }
}