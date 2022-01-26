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

import jetbrains.buildServer.dotnet.TargetsParserImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class TargetsParserTest {
    @DataProvider
    fun versionCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf("", ""),
                arrayOf(" ", ""),
                arrayOf("abc", "abc"),
                arrayOf(" abc  ", "abc"),
                arrayOf("abc;xyz", "abc;xyz"),
                arrayOf("abc; xyz", "abc;xyz"),
                arrayOf(" abc  ; xyz ", "abc;xyz"),
                arrayOf("abc xyz", "abc;xyz"),
                arrayOf("abc  xyz", "abc;xyz"),
                arrayOf(" abc  xyz  ", "abc;xyz"),
                arrayOf("\"abc\"", "abc"),
                arrayOf("\"ab c\"", "ab c"),
                arrayOf("\"ab c\";\"xy z\"", "ab c;xy z"),
                arrayOf(" \"ab c\"   \"xy z\"; ", "ab c;xy z"),
                arrayOf(" \"ab c\"   xyz ", "ab c;xyz"),
                arrayOf(" \"ab c\";xyz ", "ab c;xyz")
        )
    }

    @Test(dataProvider = "versionCases")
    fun shouldParseVersion(targets: String, expected: String) {
        // Given
        val parser = TargetsParserImpl()

        // When
        val actual = parser.parse(targets)

        // Then
        Assert.assertEquals(actual, expected)
    }
}