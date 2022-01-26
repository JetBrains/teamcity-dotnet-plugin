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

package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.ArgumentsServiceImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ArgumentsServiceTest {
    @DataProvider(name = "normalizeCases")
    fun getNormalizeCases(): Array<Array<String>> {
        return arrayOf(
                arrayOf("arg1", "arg1"),
                arrayOf("\"Arg1\"", "\"Arg1\""),
                arrayOf("\"Ar\"g1\"", "\"Ar\"g1\""),
                arrayOf("  ", "  "),
                arrayOf("", ""),
                arrayOf("arg 1", "\"arg 1\""),
                arrayOf("Arg Sss", "\"Arg Sss\""),
                arrayOf("Arg \"Sss", "\"Arg \"Sss\""),
                arrayOf("\"Arg Sss\"", "\"Arg Sss\""),
                arrayOf("\"Arg Sss\"\"", "\"Arg Sss\"\""),
                arrayOf("\"\"Arg Sss\"", "\"\"Arg Sss\""),
                arrayOf("\"Arg \"Sss\"", "\"Arg \"Sss\""))
    }

    @Test(dataProvider = "normalizeCases")
    fun shouldNormalize(arg: String, expectedArg: String) {
        // Given
        val argumentsService = createInstance()

        // When
        val actualArg = argumentsService.normalize(arg)

        // Then
        Assert.assertEquals(actualArg, expectedArg)
    }

    @DataProvider(name = "splitCases")
    fun getSplitCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("arg1 arg2", listOf("arg1", "arg2")),
                arrayOf("arg1    arg2", listOf("arg1", "arg2")),
                arrayOf("   arg1    arg2   ", listOf("arg1", "arg2")),
                arrayOf("arg1 \"arg 2\"", listOf("arg1", "arg 2")),
                arrayOf("arg1${ourLineSeparator}arg2", listOf("arg1", "arg2")),
                arrayOf(" $ourLineSeparator  arg1$ourLineSeparator $ourLineSeparator   ${ourLineSeparator}arg2 $ourLineSeparator", listOf("arg1", "arg2")),
                arrayOf("arg1 \"arg${ourLineSeparator}2\"", listOf("arg1", "arg${ourLineSeparator}2")),
                arrayOf("\"Arg 1\"${ourLineSeparator}arg2", listOf("Arg 1", "arg2")),
                arrayOf("arg1", listOf("arg1")),
                arrayOf("Arg1", listOf("Arg1")),
                arrayOf("\"arG 1\"", listOf("arG 1")),
                arrayOf("", emptyList<String>()))
    }

    @Test(dataProvider = "splitCases")
    fun shouldSplit(argsStr: String, expectedArgs: List<String>) {
        // Given
        val argumentsService = createInstance()

        // When
        val actualArgs = argumentsService.split(argsStr).toList()

        // Then
        Assert.assertEquals(actualArgs, expectedArgs)
    }

    @DataProvider(name = "combineCases")
    fun getCombineCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(listOf("arg1", "arg2"), "arg1 arg2"),
                arrayOf(listOf("\"arg 1\"", "arg2"), "\"arg 1\" arg2"),
                arrayOf(listOf("arg1"), "arg1"),
                arrayOf(listOf("arg1", "arg 2"), "arg1 \"arg 2\""),
                arrayOf(listOf("aRg1", "ArG 2"), "aRg1 \"ArG 2\""),
                arrayOf(emptyList<String>(), ""))
    }

    @Test(dataProvider = "combineCases")
    fun shouldCombine(args: List<String>, expectedArgsStr: String) {
        // Given
        val argumentsService = createInstance()

        // When
        val actualArgsStr = argumentsService.combine(args.asSequence())

        // Then
        Assert.assertEquals(actualArgsStr, expectedArgsStr)
    }

    private fun createInstance(): ArgumentsService {
        return ArgumentsServiceImpl()
    }

    companion object {
        private val ourLineSeparator = System.getProperty("line.separator")
    }
}