/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.dotnet.*
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

/**
 * @author Dmitry.Tretyakov
 * *         Date: 1/19/2016
 * *         Time: 4:01 PM
 */
class DotnetRunnerBuildServiceTest {

    @Test(dataProvider = "testBuildArgumentsData")
    fun testBuildArguments(parameters: Map<String, String>, arguments: List<String>) {
        val argumentsProvider = BuildArgumentsProvider()
        val result = argumentsProvider.getArguments(parameters)

        Assert.assertEquals(result, arguments)
    }

    @Test(dataProvider = "testRestoreArgumentsData")
    fun testRestoreArguments(parameters: Map<String, String>, arguments: List<String>) {
        val argumentsProvider = RestoreArgumentsProvider()
        val result = argumentsProvider.getArguments(parameters)

        Assert.assertEquals(result, arguments)
    }

    @Test(dataProvider = "testPublishArgumentsData")
    fun testPublishArguments(parameters: Map<String, String>, arguments: List<String>) {
        val argumentsProvider = PublishArgumentsProvider()
        val result = argumentsProvider.getArguments(parameters)

        Assert.assertEquals(result, arguments)
    }

    @Test(dataProvider = "testPackArgumentsData")
    fun testPackArguments(parameters: Map<String, String>, arguments: List<String>) {
        val argumentsProvider = PackArgumentsProvider()
        val result = argumentsProvider.getArguments(parameters)

        Assert.assertEquals(result, arguments)
    }

    @Test(dataProvider = "testTestArgumentsData")
    fun testTestArguments(parameters: Map<String, String>, arguments: List<String>) {
        val argumentsProvider = TestArgumentsProvider()
        val result = argumentsProvider.getArguments(parameters)

        Assert.assertEquals(result, arguments)
    }

    @DataProvider
    fun testBuildArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("build", "path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_BUILD_FRAMEWORK, "dnxcore50"),
                        Pair(DotnetConstants.PARAM_BUILD_CONFIG, "Release")),
                        listOf("build", "--framework", "dnxcore50", "--configuration", "Release")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_BUILD_OUTPUT, "output/"),
                        Pair(DotnetConstants.PARAM_ARGUMENTS, "--quiet")),
                        listOf("build", "--output", "output/", "--quiet")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_BUILD_NON_INCREMENTAL to " true",
                        DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES to "True "),
                        listOf("build", "--no-incremental", "--no-dependencies")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_BUILD_VERSION_SUFFIX to " preview ",
                        DotnetConstants.PARAM_VERBOSITY to "normal"),
                        listOf("build", "--version-suffix", "preview", "--verbosity", "normal")))
    }

    @DataProvider
    fun testRestoreArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("restore", "path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_RESTORE_PACKAGES, "packages/"),
                        Pair(DotnetConstants.PARAM_RESTORE_PARALLEL, "false")),
                        listOf("restore", "--packages", "packages/")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_PARALLEL, "true")), listOf("restore", "--disable-parallel")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com")),
                        listOf("restore", "--source", "http://jb.com")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com\nhttp://jb.ru")),
                        listOf("restore", "--source", "http://jb.com", "--source", "http://jb.ru")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_RESTORE_SOURCE, "http://jb.com http://jb.ru")),
                        listOf("restore", "--source", "http://jb.com", "--source", "http://jb.ru")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_RESTORE_NO_CACHE to " tRue",
                        DotnetConstants.PARAM_RESTORE_IGNORE_FAILED to "True ",
                        DotnetConstants.PARAM_RESTORE_ROOT_PROJECT to "true"),
                        listOf("restore", "--no-cache", "--ignore-failed-sources", "--no-dependencies")))
    }

    @DataProvider
    fun testPublishArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("publish", "path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_PUBLISH_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_PUBLISH_CONFIG, "Release")),
                        listOf("publish", "--framework", "dotcore", "--configuration", "Release")),
                arrayOf(mapOf(
                        DotnetConstants.PARAM_PUBLISH_RUNTIME to " active",
                        DotnetConstants.PARAM_VERBOSITY to "normal "),
                        listOf("publish", "--runtime", "active", "--verbosity", "normal")),

                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_PUBLISH_OUTPUT, "out"),
                        Pair(DotnetConstants.PARAM_PUBLISH_CONFIG, "Release")),
                        listOf("publish", "--configuration", "Release", "--output", "out")))
    }

    @DataProvider
    fun testPackArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("pack", "path/")),
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PACK_CONFIG, "Release")), listOf("pack", "--configuration", "Release")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_PACK_OUTPUT, "output/"),
                        Pair(DotnetConstants.PARAM_ARGUMENTS, "--quiet")),
                        listOf("pack", "--output", "output/", "--quiet")))
    }

    @DataProvider
    fun testTestArgumentsData(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(mapOf(Pair(DotnetConstants.PARAM_PATHS, "path/")), listOf("test", "path/")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_TEST_FRAMEWORK, "dotcore"),
                        Pair(DotnetConstants.PARAM_TEST_CONFIG, "Release")),
                        listOf("test", "--framework", "dotcore", "--configuration", "Release")),
                arrayOf(mapOf(
                        Pair(DotnetConstants.PARAM_TEST_RUNTIME, "active"),
                        Pair(DotnetConstants.PARAM_TEST_NO_BUILD, "true")),
                        listOf("test", "--runtime", "active", "--no-build")))
    }
}
