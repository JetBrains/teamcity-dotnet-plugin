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

import jetbrains.buildServer.dotnet.Version
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class VersionTest {
    @DataProvider
    fun equData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Version(5, 1, 345), Version(5, 1, 345), true),
                arrayOf(Version(5, 1), Version(5, 1), true),
                arrayOf(Version(5), Version(5), true),
                arrayOf(Version(), Version(), true),

                arrayOf(Version(1), Version(), false),
                arrayOf(Version(1), Version(2), false),
                arrayOf(Version(1, 2), Version(1), false),
                arrayOf(Version(2, 2), Version(1, 2), false),
                arrayOf(Version(1, 2, 3), Version(1, 2), false)
        )
    }

    @Test(dataProvider = "equData")
    fun shouldEqu(version1: Version, version2: Version, expectedResult: Boolean) {
        // Given

        // When
        val actualResult = version1 == version2

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    @DataProvider
    fun compareData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Version(), Version(), 0),
                arrayOf(Version(0), Version(0), 0),
                arrayOf(Version(1), Version(1), 0),
                arrayOf(Version(2), Version(1), 1),
                arrayOf(Version(2), Version(3), -1),
                arrayOf(Version(5, 1), Version(5, 1), 0),
                arrayOf(Version(5, 2), Version(5, 1), 1),
                arrayOf(Version(5, 2), Version(5, 3), -1),
                arrayOf(Version(1, 2, 3), Version(1, 2), 1),
                arrayOf(Version(1, 2), Version(1, 2, 3), -1),
                arrayOf(Version(1, 2, 3, 3), Version(1, 2), 1),
                arrayOf(Version(1, 2, 0, 0), Version(1, 2), 0),
                arrayOf(Version(1, 2, 0), Version(1, 2), 0),
                arrayOf(Version(0, 0, 0), Version(0, 0), 0),
                arrayOf(Version(0), Version(0), 0),
                arrayOf(Version(2021, 2, 0, release = "-eap01"), Version(2021, 2, 0, release = "-eap01"), 0),
                arrayOf(Version(2021, 3, 0, release = "-eap01"), Version(2021, 2, 0, release = "-eap01"), 1),
                arrayOf(Version(2021, 2, 0, release = "-eap02"), Version(2021, 2, 0, release = "-eap01"), 1),
                arrayOf(Version(2021, 2, 0, release = "-eap0.2"), Version(2021, 2, 0, release = "-eap0.3"), -1),
                arrayOf(Version(2021, 2, 0, release = "-eap01"), Version(2018, 2), 1)
        )
    }

    @Test(dataProvider = "compareData")
    fun shouldCompare(version1: Version, version2: Version, expectedResult: Int) {
        // Given

        // When
        val actualResult = version1.compareTo(version2)
        val hash1 = version1.hashCode()
        val hash2 = version2.hashCode()

        // Then
        Assert.assertEquals(actualResult, expectedResult)
        Assert.assertEquals(hash1 == hash2, expectedResult == 0)
    }

    @DataProvider
    fun parseData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("5.0", Version(5, 0)),
                arrayOf("5.1.43", Version(5, 1, 43)),
                arrayOf("5", Version(5)),
                arrayOf("2021.2.0-eap01", Version(2021, 2, 0, release = "-eap01")),
                arrayOf("2021.2.0-eap0.1", Version(2021, 2, 0, release = "-eap0.1")),

                arrayOf("", null),
                arrayOf(" ", null),
                arrayOf("abc", null),
                arrayOf("1,2", null),
                arrayOf(".5.0", null),
                arrayOf("  .5.0", null),
                arrayOf("abc.5.0", null),
                arrayOf("5.0.", null),
                arrayOf("5.0.  ", null),
                arrayOf("5.0.abc", null)
        )
    }

    @Test(dataProvider = "parseData")
    fun shouldParse(versionStr: String, expectedVersion: Version?) {
        // Given

        // When
        val actualVersion = Version.tryParse(versionStr)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }

    @Test
    fun shouldTrim() {
        // Given
        val version = Version(1, 0, 2, 0, 0)

        // When
        val trimmedVersion = version.trim()

        // Then
        Assert.assertEquals(trimmedVersion.size, 3)
    }

    @DataProvider
    fun toStingData(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(Version(5, 0), "5.0"),
                arrayOf(Version(5, 1, 43), "5.1.43"),
                arrayOf(Version(5), "5"),
                arrayOf(Version(2021, 2, 0, release = "-eap01"), "2021.2.0-eap01"),
                arrayOf(Version(2021, 2, 0, release = "-eap0.1"), "2021.2.0-eap0.1")
        )
    }

    @Test(dataProvider = "toStingData")
    fun shouldConvertToString(version: Version, expectedVersionString: String) {
        // Given

        // When
        val actualVersionString = version.toString()

        // Then
        Assert.assertEquals(actualVersionString, expectedVersionString)
    }
}