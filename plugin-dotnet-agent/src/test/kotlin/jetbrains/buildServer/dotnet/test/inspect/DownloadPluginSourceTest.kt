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

package jetbrains.buildServer.dotnet.test.inspect

import jetbrains.buildServer.E
import jetbrains.buildServer.inspect.DownloadPluginSource
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DownloadPluginSourceTest {
    @DataProvider(name = "getPluginCases")
    fun getPluginCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("Abc/123", E("Download").a("Id", "Abc").a("Version", "123")),
                arrayOf("Abc", E("Download")),
                arrayOf("Abc/123/345", E("Download")),
                arrayOf("", E("Download")),
                arrayOf("  ", E("Download"))
        )
    }

    @Test(dataProvider = "getPluginCases")
    fun shouldGetPlugin(specification: String, expectedPlugin: E) {
        // Given
        val source = createInstance()

        // When
        val aclualPlugin = source.getPlugin(specification)

        // Then
        Assert.assertEquals(aclualPlugin, expectedPlugin)
    }

    private fun createInstance() =
            DownloadPluginSource()
}