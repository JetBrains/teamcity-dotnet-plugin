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

import jetbrains.buildServer.inspect.PluginDescriptorType
import jetbrains.buildServer.inspect.PluginDescriptorType.*
import org.testng.Assert.*
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

@Test
class PluginDescriptorTypeTest {
    @DataProvider
    fun pluginDescriptorTypeTestData() = arrayOf(
        arrayOf("StyleCop.StyleCop/2021.2.1", ID, true),
        arrayOf("StyleCop.StyleCop/2021.2.1-eap", ID, true),
        arrayOf("StyleCop.StyleCop", ID, true),
        arrayOf("StyleCop.StyleCop Unity.Unity", ID, false),
        arrayOf("StyleCop. StyleCop", ID, false),
        arrayOf("Download StyleCop.StyleCop/2021.2.1", ID, false),
        arrayOf("Download StyleCop.StyleCop", ID, false),
        arrayOf("File C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3.nupkg", ID, false),
        arrayOf("File home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3.nupkg", ID, false),
        arrayOf("Folder C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3", ID, false),
        arrayOf("Folder home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3", ID, false),
        arrayOf("Folder StyleCop.StyleCop", ID, false),

        arrayOf("StyleCop.StyleCop/2021.2.1", SOURCE, false),
        arrayOf("StyleCop.StyleCop/2021.2.1-eap", SOURCE, false),
        arrayOf("StyleCop.StyleCop", SOURCE, false),
        arrayOf("StyleCop.StyleCop Unity.Unity", SOURCE, true),
        arrayOf("StyleCop. StyleCop", SOURCE, true),
        arrayOf("Download StyleCop.StyleCop/2021.2.1", SOURCE, true),
        arrayOf("Download StyleCop.StyleCop", SOURCE, true),
        arrayOf("File C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3.nupkg", SOURCE, true),
        arrayOf("File home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3.nupkg", SOURCE, true),
        arrayOf("Folder C:\\Program Files\\rs-plugins\\StyleCop.StyleCop.2021.2.3", SOURCE, true),
        arrayOf("Folder home/currentUser/rs-plugins/StyleCop.StyleCop.2021.2.3", SOURCE, true),
        arrayOf("Folder StyleCop.StyleCop", SOURCE, true),
    )

    @Test(dataProvider = "pluginDescriptorTypeTestData")
    fun `should match plugin descriptor type regex when given correct raw descriptor`(
        rawDescriptor: String,
        type: PluginDescriptorType,
        expectedMatchResult: Boolean
    ) {
        // arrange

        // act
        val actualMatchResult = type.regex.matchEntire(rawDescriptor) != null

        // assert
        assertEquals(actualMatchResult, expectedMatchResult)
    }
}