/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.dotnet.dotnet.*
import jetbrains.buildServer.dotnet.logger.DotnetLogger

/**
 * Dotnet runner service.
 */
class DotnetRunnerBuildService : BuildServiceAdapter() {

    private val myArgumentsProviders: Map<String, ArgumentsProvider> = mapOf(
            DotnetConstants.COMMAND_BUILD to BuildArgumentsProvider(),
            DotnetConstants.COMMAND_PACK to PackArgumentsProvider(),
            DotnetConstants.COMMAND_PUBLISH to PublishArgumentsProvider(),
            DotnetConstants.COMMAND_RESTORE to RestoreArgumentsProvider(),
            DotnetConstants.COMMAND_RUN to RunArgumentsProvider(),
            DotnetConstants.COMMAND_TEST to TestArgumentsProvider(),
            DotnetConstants.COMMAND_NUGET_PUSH to NugetPushArgumentsProvider(),
            DotnetConstants.COMMAND_NUGET_DELETE to NugetDeleteArgumentsProvider())

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

        val environment = DotnetUtils.updateEnvironment(environmentVariables)
        return SimpleProgramCommandLine(environment, workingDirectory.absolutePath, toolPath, arguments)
    }

    override fun getListeners(): List<ProcessListener> {
        return listOf<ProcessListener>(DotnetLogger(logger))
    }
}
