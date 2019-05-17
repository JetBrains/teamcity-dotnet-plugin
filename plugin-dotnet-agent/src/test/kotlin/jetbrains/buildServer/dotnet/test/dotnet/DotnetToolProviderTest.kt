/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetToolProvider
import jetbrains.buildServer.dotnet.test.agent.ToolSearchServiceStub
import jetbrains.buildServer.util.OSType
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetToolProviderTest {
    private lateinit var _ctx: Mockery
    private lateinit var _toolProvidersRegistry: ToolProvidersRegistry
    private lateinit var _toolSearchService: ToolSearchService
    private lateinit var _environment: Environment
    private lateinit var _parametersService: ParametersService

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _toolProvidersRegistry = _ctx.mock(ToolProvidersRegistry::class.java)
        _toolSearchService = _ctx.mock(ToolSearchService::class.java)
        _environment = _ctx.mock(Environment::class.java)
        _parametersService = _ctx.mock(ParametersService::class.java)
    }

    @DataProvider
    fun supportToolCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("dotnet.cli", true),
                arrayOf("DoTnet.cli", true),
                arrayOf("DOTNET.cli", true),
                arrayOf("DOTNET2.cli", false),
                arrayOf("abc", false),
                arrayOf(" dotnet ", false),
                arrayOf("   ", false),
                arrayOf("", false),
                arrayOf("dotnet.exe", false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldSupportTool(toolName: String, expectedResult: Boolean) {
        // Given
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ToolProvidersRegistry>(_toolProvidersRegistry).registerToolProvider(with(any(ToolProvider::class.java)))
            }
        })
        val toolProvider = createInstance(emptySequence())

        // When
        val actualResult = toolProvider.supports(toolName)

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualResult, expectedResult)
    }

    @DataProvider
    fun getPathCases(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf(
                        sequenceOf(File("path1"), File("path1")),
                        File("path1").absoluteFile,
                        false),
                arrayOf(
                        sequenceOf(File("path2")),
                        File("path2").absoluteFile,
                        false),
                arrayOf(
                        emptySequence<File>(),
                        File("a").absoluteFile,
                        true))
    }

    @Test(dataProvider = "getPathCases")
    fun shouldGetPath(
            files: Sequence<File>,
            expectedPath: File,
            expectedToolCannotBeFoundException: Boolean) {
        // Given
        _ctx.checking(object : Expectations() {
            init {
                oneOf<ToolProvidersRegistry>(_toolProvidersRegistry).registerToolProvider(with(any(ToolProvider::class.java)))

                oneOf<Environment>(_environment).os
                will(returnValue(OSType.WINDOWS))
            }
        })
        val toolProvider = createInstance(files)

        var actualToolCannotBeFoundException = false

        // When
        var actualPath = ""
        try {
            actualPath = toolProvider.getPath("tool")
        } catch (ex: ToolCannotBeFoundException) {
            actualToolCannotBeFoundException = true
        }


        // Then
        if (!expectedToolCannotBeFoundException) {
            Assert.assertEquals(actualPath, expectedPath.absolutePath)
        }

        Assert.assertEquals(actualToolCannotBeFoundException, expectedToolCannotBeFoundException)
    }

    @Test
    fun shouldNotSearchToolInVirtualContext() {
        val build = _ctx.mock(AgentRunningBuild::class.java)
        val context = _ctx.mock(BuildRunnerContext::class.java)
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_toolProvidersRegistry)!!.registerToolProvider(with(any(DotnetToolProvider::class.java)))

                allowing(context).isVirtualContext
                will(returnValue(true))
            }
        })

        val toolProvider = createInstance(emptySequence())
        val path = toolProvider.getPath("dotnet.cli", build, context)

        Assert.assertEquals(path, "dotnet")
    }

    @DataProvider
    fun testDataAdditionalPath(): Array<Array<Any>> {
        return arrayOf(
                arrayOf(OSType.WINDOWS, File("C:\\Program Files\\dotnet")),
                arrayOf(OSType.UNIX, File("/usr/share/dotnet")),
                arrayOf(OSType.MAC, File("/usr/local/share/dotnet")))
    }

    @Test(dataProvider = "testDataAdditionalPath")
    fun shouldProvideAdditionalPath(os: OSType, expectedPath: File) {
        // Given
        _ctx.checking(object : Expectations() {
            init {
                oneOf(_toolProvidersRegistry)!!.registerToolProvider(with(any(DotnetToolProvider::class.java)))

                oneOf<Environment>(_environment).os
                will(returnValue(os))
            }
        })

        val toolProvider = createInstance(emptySequence())

        // When
        val actualPath = toolProvider.additionalPath

        // Then
        _ctx.assertIsSatisfied()
        Assert.assertEquals(actualPath, expectedPath)
    }

    private fun createInstance(files: Sequence<File>): DotnetToolProvider =
            DotnetToolProvider(
                    _toolProvidersRegistry,
                    ToolSearchServiceStub(files),
                    _environment)
}