package jetbrains.buildServer.dotnet.test.mono

import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.mono.MonoVersionParser
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class MonoVersionParserTest {
    @DataProvider
    fun versionCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(sequenceOf(
                        "Mono JIT compiler version 5.2.0 (Visual Studio built mono)",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com",
                        "        TLS:           normal",
                        "        SIGSEGV:       normal",
                        "        Notification:  Thread + polling"),
                        Version(5, 2,0)),
                arrayOf(sequenceOf(
                        "Mono version 5.0.1234",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com"),
                        "5.0.1234"),
                arrayOf(sequenceOf(
                        "Mono JIT compiler 5.2.0 (Visual Studio built mono)",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com",
                        "        TLS:           normal",
                        "        SIGSEGV:       normal",
                        "        Notification:  Thread + polling"),
                        Version.Empty),
                arrayOf(sequenceOf("   "), null),
                arrayOf(sequenceOf(""), null),
                arrayOf(emptySequence<String>(), null),
                arrayOf(sequenceOf(
                        "Mono",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com",
                        "        TLS:           normal",
                        "        SIGSEGV:       normal",
                        "        Notification:  Thread + polling"),
                        Version.Empty))
    }

    @Test(dataProvider = "versionCases")
    fun shouldParseVersion(output: Sequence<String>, expectedVersion: Version) {
        // Given
        val parser = MonoVersionParser()

        // When
        val actualVersion = parser.parse(output)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }
}