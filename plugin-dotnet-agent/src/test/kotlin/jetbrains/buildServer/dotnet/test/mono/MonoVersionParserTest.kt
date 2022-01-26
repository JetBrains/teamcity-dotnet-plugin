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

package jetbrains.buildServer.dotnet.test.mono

import jetbrains.buildServer.agent.Version
import jetbrains.buildServer.mono.MonoVersionParser
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MonoVersionParserTest {
    @DataProvider
    fun versionCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(listOf(
                        "Mono JIT compiler version 5.2.0 (Visual Studio built mono)",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com",
                        "        TLS:           normal",
                        "        SIGSEGV:       normal",
                        "        Notification:  Thread + polling"),
                        Version(5, 2, 0)),
                arrayOf(listOf(
                        "Mono version 5.0.1234",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com"),
                        Version(5, 0, 1234)),
                arrayOf(listOf(
                        "Mono JIT compiler 5.2.0 (Visual Studio built mono)",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com",
                        "        TLS:           normal",
                        "        SIGSEGV:       normal",
                        "        Notification:  Thread + polling"),
                        Version.Empty),
                arrayOf(listOf("   "), Version.Empty),
                arrayOf(listOf(""), Version.Empty),
                arrayOf(emptyList<String>(), Version.Empty),
                arrayOf(listOf(
                        "Mono",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com",
                        "        TLS:           normal",
                        "        SIGSEGV:       normal",
                        "        Notification:  Thread + polling"),
                        Version.Empty))
    }

    @Test(dataProvider = "versionCases")
    fun shouldParseVersion(output: Collection<String>, expectedVersion: Version) {
        // Given
        val parser = MonoVersionParser()

        // When
        val actualVersion = parser.parse(output)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }
}