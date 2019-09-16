/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.dotnet.DotnetVersionParser
import jetbrains.buildServer.dotnet.Version
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetVersionParserTest {
    @DataProvider
    fun versionCases(): Array<Array<out Any?>> {
        return arrayOf(
                arrayOf(listOf("1.0.0-preview2-003133"), Version.parse("1.0.0-preview2-003133")),
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