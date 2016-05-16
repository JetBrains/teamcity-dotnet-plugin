/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.dotnet.dnu.BuildArgumentsProvider;
import jetbrains.buildServer.dotnet.dnu.PackArgumentsProvider;
import jetbrains.buildServer.dotnet.dnu.PublishArgumentsProvider;
import jetbrains.buildServer.dotnet.dnu.RestoreArgumentsProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dnu runner service.
 */
public class DnuRunnerBuildService extends BuildServiceAdapter {

    private final Map<String, ArgumentsProvider> myArgumentsProviders;

    public DnuRunnerBuildService() {
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

        final String commandName = parameters.get(DnuConstants.PARAM_COMMAND);
        if (StringUtil.isEmpty(commandName)) {
            throw new RunBuildException("DNU command name is empty");
        }

        final ArgumentsProvider argumentsProvider = myArgumentsProviders.get(commandName);
        if (argumentsProvider == null) {
            throw new RunBuildException("Unable to construct arguments for DNU command " + commandName);
        }

        final List<String> arguments = argumentsProvider.getArguments(parameters);
        final String toolPath;
        try {
            toolPath = getToolPath(DnuConstants.RUNNER_TYPE);
        } catch (ToolCannotBeFoundException e) {
            final RunBuildException exception = new RunBuildException(e);
            exception.setLogStacktrace(false);
            throw exception;
        }

        return createProgramCommandline(toolPath, arguments);
    }
}
