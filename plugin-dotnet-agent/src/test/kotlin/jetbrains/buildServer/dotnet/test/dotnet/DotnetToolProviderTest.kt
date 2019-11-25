/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test.dotnet

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import jetbrains.buildServer.agent.Path
import jetbrains.buildServer.agent.ToolEnvironment
import jetbrains.buildServer.agent.ToolProvidersRegistry
import jetbrains.buildServer.agent.ToolSearchService
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetSdksProvider
import jetbrains.buildServer.dotnet.DotnetToolProvider
import jetbrains.buildServer.dotnet.test.agent.ToolSearchServiceStub
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class DotnetToolProviderTest {
    @MockK private lateinit var _toolProvidersRegistry: ToolProvidersRegistry
    @MockK private lateinit var _toolSearchService: ToolSearchService
    @MockK private lateinit var _executablePathsProvider: ToolEnvironment
    @MockK private lateinit var _parametersService: ParametersService
    @MockK private lateinit var _dotnetSdksProvider: DotnetSdksProvider

    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
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
        every { _toolProvidersRegistry.registerToolProvider(any()) } returns Unit
        val toolProvider = createInstance(emptySequence())

        // When
        val actualResult = toolProvider.supports(toolName)

        // Then
        Assert.assertEquals(actualResult, expectedResult)
    }

    private fun createInstance(paths: Sequence<Path>): DotnetToolProvider =
            DotnetToolProvider(
                    _toolProvidersRegistry,
                    ToolSearchServiceStub(paths),
                    _executablePathsProvider,
                    _dotnetSdksProvider)
}