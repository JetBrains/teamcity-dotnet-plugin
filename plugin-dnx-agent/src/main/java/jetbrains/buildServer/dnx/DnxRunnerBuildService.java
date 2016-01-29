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
import jetbrains.buildServer.dnx.arguments.ArgumentsProvider;
import jetbrains.buildServer.dnx.arguments.DnxArgumentsProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Dnx runner service.
 */
public class DnxRunnerBuildService extends BuildServiceAdapter {

    private final ArgumentsProvider myArgumentsProvider;

    public DnxRunnerBuildService() {
        myArgumentsProvider = new DnxArgumentsProvider();
    }

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Map<String, String> parameters = getRunnerParameters();
        final List<String> arguments = myArgumentsProvider.getArguments(parameters);
        final String toolPath = getToolPath(DnxConstants.RUNNER_TYPE);

        return createProgramCommandline(toolPath, arguments);
    }
}
