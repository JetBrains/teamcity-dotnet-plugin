/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetUtils
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/**
 * @author Dmitry.Tretyakov
 *         Date: 29.10.2016
 *         Time: 10:17
 */
class DotnetUtilsTest {
    @Test(dataProvider = "getSdkVersion")
    fun getSdkVersion(output: String, version: String) {
        val result = DotnetUtils.getSdkVersion(output)

        Assert.assertEquals(result, version)
    }

    @DataProvider
    fun getSdkVersion(): Array<Array<String>> {
        return arrayOf(
                arrayOf("1.0.0-preview2-003133", "1.0.0-preview2-003133"),
                arrayOf("Product Information:\n Version:     1.0.0-beta-001598\n Commit Sha:  7582649f88", "1.0.0-beta-001598"))
    }

}