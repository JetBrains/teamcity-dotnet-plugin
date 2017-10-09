/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetVersionParser
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetVersionParserTest {
    @DataProvider
    fun versionCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(sequenceOf("1.0.0-preview2-003133"), "1.0.0-preview2-003133"),
                arrayOf(sequenceOf("2.0.0"), "2.0.0"),
                arrayOf(sequenceOf("2.0.1234"), "2.0.1234"),
                arrayOf(sequenceOf("   2.0.1234    "), "2.0.1234"),
                arrayOf(sequenceOf("2.0"), null),
                arrayOf(sequenceOf("2"), null),
                arrayOf(sequenceOf(""), null),
                arrayOf(sequenceOf("  "), null),
                arrayOf(emptySequence<String>(), null),
                arrayOf(sequenceOf(
                        "Product Information:",
                        " Version:     1.0.0-beta-001598",
                        " Commit Sha:  7582649f88"),
                        "1.0.0-beta-001598"))
    }

    @Test(dataProvider = "versionCases")
    fun shouldParseVersion(output: Sequence<String>, expectedVersion: String?) {
        // Given
        val parser = DotnetVersionParser()

        // When
        val actualVersion = parser.tryParse(output)

        // Then
        Assert.assertEquals(actualVersion, expectedVersion)
    }
}