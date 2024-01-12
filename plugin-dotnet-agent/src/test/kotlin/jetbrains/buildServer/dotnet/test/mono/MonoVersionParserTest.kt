

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