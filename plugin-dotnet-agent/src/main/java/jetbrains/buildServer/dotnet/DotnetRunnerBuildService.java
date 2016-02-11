/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.dotnet.dotnet.BuildArgumentsProvider;
import jetbrains.buildServer.dotnet.dotnet.PackArgumentsProvider;
import jetbrains.buildServer.dotnet.dotnet.PublishArgumentsProvider;
import jetbrains.buildServer.dotnet.dotnet.RestoreArgumentsProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dotnet runner service.
 */
public class DotnetRunnerBuildService extends BuildServiceAdapter {

    private final Map<String, ArgumentsProvider> myArgumentsProviders;

    public DotnetRunnerBuildService() {
        myArgumentsProviders = new HashMap<String, ArgumentsProvider>();
        myArgumentsProviders.put(DnuConstants.COMMAND_BUILD, new BuildArgumentsProvider());
        myArgumentsProviders.put(DnuConstants.COMMAND_PACK, new PackArgumentsProvider());
        myArgumentsProviders.put(DnuConstants.COMMAND_PUBLISH, new PublishArgumentsProvider());
        myArgumentsProviders.put(DnuConstants.COMMAND_RESTORE, new RestoreArgumentsProvider());
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Map<String, String> parameters = getRunnerParameters();

        final String commandName = parameters.get(DotnetConstants.PARAM_COMMAND);
        if (StringUtil.isEmpty(commandName)) {
            throw new RunBuildException("Dotnet command name is empty");
        }

        final ArgumentsProvider argumentsProvider = myArgumentsProviders.get(commandName);
        if (argumentsProvider == null) {
            throw new RunBuildException("Unable to construct arguments for dotnet command " + commandName);
        }

        final List<String> arguments = argumentsProvider.getArguments(parameters);
        final String toolPath = getToolPath(DotnetConstants.RUNNER_TYPE);

        return createProgramCommandline(toolPath, arguments);
    }
}
