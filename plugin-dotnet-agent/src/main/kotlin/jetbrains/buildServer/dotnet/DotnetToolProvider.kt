/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.*

/**
 * Lookups for .NET CLI utilities.
 */
class DotnetToolProvider(
        toolProvidersRegistry: ToolProvidersRegistry,
        private val _toolSearchService: ToolSearchService)
    : ToolProvider {
    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = DotnetConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String =
            _toolSearchService.find(DotnetConstants.TOOL_HOME, Targets)
                    .firstOrNull()
                    ?.absolutePath
                    ?: throw ToolCannotBeFoundException("""
                    Unable to locate tool $toolName in the system. Please make sure that `PATH` variable contains
                    .NET CLI toolchain directory or defined `${DotnetConstants.TOOL_HOME}` variable.""")

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String = getPath(toolName)

    companion object {
        internal val Targets = sequenceOf("dotnet.exe", "dotnet")
    }
}