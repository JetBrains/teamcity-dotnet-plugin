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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DNX utility runner service.
 */
public class DnuRunnerBuildService extends BuildServiceAdapter {

    @NotNull
    @Override
    public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
        final Map<String, String> parameters = getRunnerParameters();
        final List<String> arguments = new ArrayList<String>();

        arguments.add(parameters.get(DnuConstants.DNU_PARAM_COMMAND));

        final String projectsValue = parameters.get(DnuConstants.DNU_PARAM_PROJECTS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.add(projectsValue.trim());
        }

        final String packagesValue = parameters.get(DnuConstants.DNU_PARAM_PACKAGES);
        if (!StringUtil.isEmptyOrSpaces(packagesValue)){
            arguments.add("--packages " + packagesValue.trim());
        }

        final String parallelValue = parameters.get(DnuConstants.DNU_PARAM_PARALLEL);
        if ("true".equalsIgnoreCase(parallelValue)){
            arguments.add("--parallel");
        }

        final String argumentsValue = parameters.get(DnuConstants.DNU_PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)){
            arguments.add(argumentsValue.trim());
        }

        return createProgramCommandline(getToolPath(DnxToolProvider.DNU_TOOL), arguments);
    }
}
