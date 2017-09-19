package jetbrains.buildServer.dotnet.test.agent

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.ArgumentsServiceImpl
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ArgumentsServiceTest {
    @DataProvider(name = "splitCases")
    fun getSplitCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("arg1 arg2", listOf("arg1", "arg2")),
                arrayOf("arg1    arg2", listOf("arg1", "arg2")),
                arrayOf("   arg1    arg2   ", listOf("arg1", "arg2")),
                arrayOf("arg1 \"arg 2\"", listOf("arg1", "arg 2")),
                arrayOf("arg1${ourLineSeparator}arg2", listOf("arg1", "arg2")),
                arrayOf(" ${ourLineSeparator}  arg1${ourLineSeparator} ${ourLineSeparator}   ${ourLineSeparator}arg2 ${ourLineSeparator}", listOf("arg1", "arg2")),
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
        val actualArgs = argumentsService.split(argsStr).toList();

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
        val actualArgsStr = argumentsService.combine(args.asSequence());

        // Then
        Assert.assertEquals(actualArgsStr, expectedArgsStr)
    }

    @DataProvider(name = "escapeCases")
    fun getEscapeCases(): Array<Array<String>> {
        return arrayOf(
                arrayOf("arg1", "arg1"))
    }

    @Test(dataProvider = "escapeCases")
    fun shouldCombine(argsStr: String, expectedArgsStr: String) {
        // Given
        val argumentsService = createInstance()

        // When
        val actualArgsStr = argumentsService.escape(argsStr);

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