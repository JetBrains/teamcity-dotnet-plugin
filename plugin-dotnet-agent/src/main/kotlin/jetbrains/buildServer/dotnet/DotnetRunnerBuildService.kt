/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.dotnet.*
import java.util.*

/**
 * Dotnet runner service.
 */
class DotnetRunnerBuildService : BuildServiceAdapter() {

    private val myArgumentsProviders: Map<String, ArgumentsProvider>

    init {
        myArgumentsProviders = mapOf(
                Pair(DotnetConstants.COMMAND_BUILD, BuildArgumentsProvider()),
                Pair(DotnetConstants.COMMAND_PACK, PackArgumentsProvider()),
                Pair(DotnetConstants.COMMAND_PUBLISH, PublishArgumentsProvider()),
                Pair(DotnetConstants.COMMAND_RESTORE, RestoreArgumentsProvider()),
                Pair(DotnetConstants.COMMAND_TEST, TestArgumentsProvider()))
    }

    @Throws(RunBuildException::class)
    override fun makeProgramCommandLine(): ProgramCommandLine {
        val parameters = runnerParameters

        val commandName = parameters[DotnetConstants.PARAM_COMMAND]
        if (commandName.isNullOrBlank()) {
            throw RunBuildException("Dotnet command name is empty")
        }

        val argumentsProvider = myArgumentsProviders[commandName] ?: throw RunBuildException("Unable to construct arguments for dotnet command $commandName")

        val arguments = argumentsProvider.getArguments(parameters)
        val toolPath: String
        try {

            toolPath = getToolPath(DotnetConstants.RUNNER_TYPE)
        } catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException(e)
            exception.isLogStacktrace = false
            throw exception
        }

        // disable telemetry data collection: https://aka.ms/dotnet-cli-telemetry
        val environment = HashMap(environmentVariables)
        environment["DOTNET_CLI_TELEMETRY_OPTOUT"] = "true"

        // disable showing eula and first time package caching
        environment["DOTNET_SKIP_FIRST_TIME_EXPERIENCE"] = "true"

        // skip xml docs download for restored packages
        environment["NUGET_XMLDOC_MODE"] = "skip"

        return SimpleProgramCommandLine(environment, workingDirectory.absolutePath, toolPath, arguments)
    }
}
