/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
import jetbrains.buildServer.agent.runner.ProgramCommandLine;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DNX utility runner service.
 */
public class DnuRunnerBuildService extends BuildServiceAdapter {

    private final Map<String, ArgumentsProvider> myArgumentsProviders;

    public DnuRunnerBuildService(){
        myArgumentsProviders = new HashMap<String, ArgumentsProvider>();
        myArgumentsProviders.put(DnuConstants.DNU_COMMAND_BUILD, new DnuBuildArgumentsProvider());
        myArgumentsProviders.put(DnuConstants.DNU_COMMAND_RESTORE, new DnuRestoreArgumentsProvider());
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Map<String, String> parameters = getRunnerParameters();

        final String commandName = parameters.get(DnuConstants.DNU_PARAM_COMMAND);
        if (StringUtil.isEmpty(commandName)){
            throw new RunBuildException("DNU command name is empty");
        }

        final ArgumentsProvider argumentsProvider = myArgumentsProviders.get(commandName);
        if (argumentsProvider == null){
            throw new RunBuildException("Unable to construct arguments for DNU command " + commandName);
        }

        final List<String> arguments = argumentsProvider.getArguments(parameters);

        return createProgramCommandline(getToolPath(DnxToolProvider.DNU_TOOL), arguments);
    }
}
