/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test.dotnet

import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.agent.ToolSearchService
import jetbrains.buildServer.dotnet.DotnetToolProvider
import jetbrains.buildServer.dotnet.test.agent.ToolSearchServiceStub
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.io.File

class DotnetToolProviderTest {
    private var _ctx: Mockery? = null
    private var _toolProvidersRegistry: ToolProvidersRegistry? = null
    private var _toolSearchService: ToolSearchService? = null

    @BeforeMethod
    fun setUp() {
        _ctx = Mockery()
        _toolProvidersRegistry = _ctx!!.mock(ToolProvidersRegistry::class.java)
        _toolSearchService = _ctx!!.mock(ToolSearchService::class.java)
    }

    @DataProvider
    fun supportToolCases(): Array<Array<Any>> {
        return arrayOf(
                arrayOf("dotnet", true),
                arrayOf("DoTnet", true),
                arrayOf("DOTNET", true),
                arrayOf("DOTNET2", false),
                arrayOf("abc", false),
                arrayOf(" dotnet ", false),
                arrayOf("   ", false),
                arrayOf("", false))
    }

    @Test(dataProvider = "supportToolCases")
    fun shouldSupportTool(toolName: String, expectedResult: Boolean) {
        // Given
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ToolProvidersRegistry>(_toolProvidersRegistry).registerToolProvider(with(any(ToolProvider::class.java)))
            }
        })
        val toolProvider = createInstance(emptySequence())

        // When
        val actualResult = toolProvider.supports(toolName)

        // Then
        _ctx!!.assertIsSatisfied()
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
        _ctx!!.checking(object : Expectations() {
            init {
                oneOf<ToolProvidersRegistry>(_toolProvidersRegistry).registerToolProvider(with(any(ToolProvider::class.java)))
            }
        })
        val toolProvider = createInstance(files)

        var actualToolCannotBeFoundException = false

        // When
        var actualPath = ""
        try {
            actualPath = toolProvider.getPath("tool")
        }
        catch (ex: ToolCannotBeFoundException) {
            actualToolCannotBeFoundException = true
        }


        // Then
        if(!expectedToolCannotBeFoundException) {
            Assert.assertEquals(actualPath, expectedPath.absolutePath)
        }

        Assert.assertEquals(actualToolCannotBeFoundException, expectedToolCannotBeFoundException)
    }

    private fun createInstance(files: Sequence<File>): ToolProvider =
            DotnetToolProvider(
                    _toolProvidersRegistry!!,
                    ToolSearchServiceStub(files))
}