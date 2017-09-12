package jetbrains.buildServer.dotnet.test.mono

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
                        "5.2.0"),
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
                        null),
                arrayOf(sequenceOf("   "), null),
                arrayOf(sequenceOf(""), null),
                arrayOf(emptySequence<String>(), null),
                arrayOf(sequenceOf(
                        "Mono",
                        "Copyright (C) 2002-2014 Novell, Inc, Xamarin Inc and Contributors. www.mono-project.com",
                        "        TLS:           normal",
                        "        SIGSEGV:       normal",
                        "        Notification:  Thread + polling"),
                        null))
    }

    @Test(dataProvider = "versionCases")
    fun shouldParseVersion(output: Sequence<String>, expectedVersion: String?) {
        // Given
        val parser = MonoVersionParser()

        // When
        val actualVersion = parser.tryParse(output)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }
}