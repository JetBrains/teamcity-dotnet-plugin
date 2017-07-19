/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.StringUtil

import java.io.File
import java.util.regex.Pattern

/**
 * Lookups for .NET CLI utilities.
 */
class DotnetToolProvider(toolProvidersRegistry: ToolProvidersRegistry) : ToolProvider {
    private val LOG = Logger.getInstance(DotnetToolProvider::class.java.name)

    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = DotnetConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String {
        val pathVariable = System.getenv(PATH_VARIABLE)
        val paths = StringUtil.splitHonorQuotes(pathVariable, File.pathSeparatorChar)

        // Try to use DOTNET_HOME variable
        val dotnetHomeVariable = System.getenv(DotnetConstants.TOOL_HOME)
        if (!dotnetHomeVariable.isNullOrBlank()) {
            paths.add(0, dotnetHomeVariable)
        }

        return findToolPath(paths, TOOL_PATTERN) ?:
                throw ToolCannotBeFoundException("""
                Unable to locate tool $toolName in the system. Please make sure that `$PATH_VARIABLE` variable contains
                .NET CLI toolchain directory or defined `$DotnetConstants.TOOL_HOME` variable.""")
    }

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String,
                         build: AgentRunningBuild,
                         runner: BuildRunnerContext): String {
        return getPath(toolName)
    }

    /**
     * Returns a first matching file in the list of directories.

     * @param paths   list of directories.
     * *
     * @param pattern pattern.
     * *
     * @return first matching file.
     */
    private fun findToolPath(paths: List<String>, pattern: Pattern): String? {
        for (path in paths) {
            val directory = File(path)
            if (!directory.exists()) {
                LOG.debug("Ignoring non existing directory $path")
                continue
            }

            val files = directory.listFiles()
            if (files == null) {
                LOG.debug("Ignoring empty directory $path")
                continue
            }

            for (file in files) {
                val absolutePath = file.absolutePath
                if (pattern.matcher(absolutePath).find()) {
                    return absolutePath
                }
            }
        }

        return null
    }

    companion object {
        private val TOOL_PATTERN = Pattern.compile("^.*${DotnetConstants.RUNNER_TYPE}(\\.(exe))?$")
        private val PATH_VARIABLE = "PATH"
    }
}
