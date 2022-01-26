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

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.SemanticVersion
import jetbrains.buildServer.dotnet.SemanticVersionParser
import jetbrains.buildServer.dotnet.SemanticVersionParserImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class SemanticVersionParserTest {
    @DataProvider
    fun getProjectFiles(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf<Any?>("3.2.4", SemanticVersion(3, 2, 4)),
                arrayOf<Any?>("11133.42.444", SemanticVersion(11133, 42, 444)),
                arrayOf<Any?>("3.2.4-Beta", SemanticVersion(3, 2, 4, "Beta")),
                arrayOf<Any?>("333.222.54-beTa", SemanticVersion(333, 222, 54, "beTa")),
                arrayOf<Any?>("3.2.4-rc", SemanticVersion(3, 2, 4, "rc")),
                arrayOf<Any?>("2021.2.0-eap01", SemanticVersion(2021, 2, 0, "eap01")),
                arrayOf<Any?>("2021.2.0-eap0.1", SemanticVersion(2021, 2, 0, "eap0.1")),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", SemanticVersion(123, 0, 18, "BEta")),
                arrayOf<Any?>("path\\TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", null),
                arrayOf<Any?>("path/TeamCity.Dotnet.Integration.123.0.18-BEta.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.18.nupkg", SemanticVersion(41, 30, 18)),
                arrayOf<Any?>("Integration.41.30.18.nupkg", SemanticVersion(41, 30, 18)),
                arrayOf<Any?>("TeamCity.csi.1.23.4-beta1.nupkg", SemanticVersion(1, 23, 4, "beta1")),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.18.33.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.18.33-rc.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.30-Beta.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41.nupkg", null),
                arrayOf<Any?>("TeamCity.Dotnet.Integration.41-aa.nupkg", null),
                arrayOf<Any?>("3.2.4#rc", null),
                arrayOf<Any?>("3.2.4-", null),
                arrayOf<Any?>("3.2.4- abc ", null),
                arrayOf<Any?>("3.2.4-a b", null),
                arrayOf<Any?>("", null),
                arrayOf<Any?>("abc", null),
                arrayOf<Any?>("3", null),
                arrayOf<Any?>("3-abc", null),
                arrayOf<Any?>("3.4", null),
                arrayOf<Any?>("3.4-ddd", null),
                arrayOf<Any?>("3.2.4.6", null),
                arrayOf<Any?>("1.0.4", SemanticVersion(1, 0, 4)),
                arrayOf<Any?>("NuGetFallbackFolder", null)
        )
    }

    @Test(dataProvider = "getProjectFiles")
    fun shouldParsePackageVersion(versionString: String, expectedPackageVersion: SemanticVersion?) {
        // Given
        val parser = createInstance()

        // When
        val actualPackageVersion = parser.tryParse(versionString)

        // Then
        Assert.assertEquals(actualPackageVersion, expectedPackageVersion)
    }

    private fun createInstance(): SemanticVersionParser {
        return SemanticVersionParserImpl()
    }
}