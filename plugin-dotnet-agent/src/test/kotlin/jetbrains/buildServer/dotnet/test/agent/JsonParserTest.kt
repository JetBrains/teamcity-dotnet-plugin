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

package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.JsonParserImpl
import jetbrains.buildServer.visualStudio.JsonVisualStudioInstanceParser
import org.testng.Assert
import org.testng.annotations.Test
import java.io.*

class JsonParserTest {
    @Test
    fun shouldParse() {
        // Given
        var stateFile = File(JsonParserTest::class.java.classLoader.getResource("vs2019_state.json")!!.file)

        // When
        FileInputStream(stateFile).use {
            BufferedReader(InputStreamReader(it)).use {
                val state = JsonParserImpl().tryParse<JsonVisualStudioInstanceParser.VisualStudioState>(it, JsonVisualStudioInstanceParser.VisualStudioState::class.java)

                // Then
                Assert.assertNotNull(state)
                Assert.assertEquals(state?.installationPath, "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Professional")
                Assert.assertEquals(state?.catalogInfo?.productDisplayVersion, "16.5.4")
                Assert.assertEquals(state?.catalogInfo?.productLineVersion, "2019")
            }
        }
    }
}