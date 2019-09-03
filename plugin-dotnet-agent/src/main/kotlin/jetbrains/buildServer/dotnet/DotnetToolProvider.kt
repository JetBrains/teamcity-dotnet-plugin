/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.OSType
import java.io.File

/**
 * Lookups for .NET CLI utilities.
 */
class DotnetToolProvider(
        toolProvidersRegistry: ToolProvidersRegistry,
        private val _toolSearchService: ToolSearchService,
        private val _environment: Environment,
        private val _dotnetCliToolInfo: DotnetCliToolInfo)
    : ToolProvider {
    init {
        toolProvidersRegistry.registerToolProvider(this)
    }

    override fun supports(toolName: String): Boolean = DotnetConstants.RUNNER_TYPE.equals(toolName, ignoreCase = true)

    override fun getPath(toolName: String): String =
        executablePath
                ?.absolutePath
                ?: throw ToolCannotBeFoundException("""
                        Unable to locate tool $toolName in the system. Please make sure that `PATH` variable contains
                        .NET CLI toolchain directory or defined `${DotnetConstants.TOOL_HOME}` variable.""".trimIndent())

    val additionalPath: File get() = when(_environment.os) {
        OSType.WINDOWS -> _environment.tryGetVariable(DotnetConstants.PROGRAM_FILES_ENV_VAR)
                ?.let {
                    File(it, DotnetConstants.DOTNET_DEFAULT_DIRECTORY)
                }
                ?: File("C:\\Program Files\\${DotnetConstants.DOTNET_DEFAULT_DIRECTORY}")
        OSType.UNIX -> File("/usr/share/dotnet")
        OSType.MAC -> File("/usr/local/share/dotnet")
    }

    private val executablePath: File? by lazy {
        val executables = _toolSearchService.find(DotnetConstants.EXECUTABLE, DotnetConstants.TOOL_HOME, sequenceOf(additionalPath)).distinct()
        var dotnetRuntime: File? = null
        for (dotnetExecutable in executables) {
            if (_dotnetCliToolInfo.getSdks(dotnetExecutable).any()) {
                return@lazy dotnetExecutable
            }
            else {
                LOG.debug("Cannot find .NET Core SDK for <${dotnetExecutable}>.")
                dotnetRuntime = dotnetExecutable
            }
        }

        dotnetRuntime?.let {
            LOG.warn(".NET Core SDK was not found (found .NET Core runtime at path <${it.absolutePath}>). Install .NET Core SDK into the default location or set ${DotnetConstants.TOOL_HOME} environment variable pointing to the installation directory.")
        }

        return@lazy null
    }

    @Throws(ToolCannotBeFoundException::class)
    override fun getPath(toolName: String, build: AgentRunningBuild, runner: BuildRunnerContext): String {
        return if (runner.isVirtualContext) {
            DotnetConstants.EXECUTABLE
        } else {
            getPath(toolName)
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotnetToolProvider::class.java.name)
    }
}