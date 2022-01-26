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

import jetbrains.buildServer.dotnet.DotnetVersionParser
import jetbrains.buildServer.agent.Version
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetVersionParserTest {
    @DataProvider
    fun versionCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(listOf("5.0.100-preview.1.20155.7"), Version(5, 0, 100, "preview.1.20155.7")),
                arrayOf(listOf("1.0.0-preview2-003133"), Version(1, 0, 0, "preview2-003133")),
                arrayOf(listOf("2.0.0"), Version(2, 0, 0)),
                arrayOf(listOf("2.0.1234"), Version(2, 0, 1234)),
                arrayOf(listOf("   2.0.1234    "), Version(2, 0, 1234)),
                arrayOf(listOf("2.0"), Version.Empty),
                arrayOf(listOf("2"), Version.Empty),
                arrayOf(listOf(""), Version.Empty),
                arrayOf(listOf("  "), Version.Empty),
                arrayOf(listOf<String>(), Version.Empty),
                arrayOf(listOf(
                        "Product Information:",
                        " Version:     1.0.0-beta-001598",
                        " Commit Sha:  7582649f88"),
                        Version.parse("1.0.0-beta-001598")),
                arrayOf(listOf("2.1.500-preview-009335"), Version.parse("2.1.500-preview-009335")))
    }

    @Test(dataProvider = "versionCases")
    fun shouldParseVersion(output: Collection<String>, expectedVersion: Version) {
        // Given
        val parser = DotnetVersionParser()

        // When
        val actualVersion = parser.parse(output)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }
}