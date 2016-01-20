/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides arguments to dnu build command.
 */
public class DnuBuildArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DnuConstants.DNU_COMMAND_BUILD);

        final String projectsValue = parameters.get(DnuConstants.DNU_PARAM_BUILD_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.add(projectsValue.trim());
        }

        final String frameworkValue = parameters.get(DnuConstants.DNU_PARAM_BUILD_FRAMEWORK);
        if (!StringUtil.isEmptyOrSpaces(frameworkValue)) {
            arguments.add("--framework");
            arguments.add(frameworkValue.trim());
        }

        final String configValue = parameters.get(DnuConstants.DNU_PARAM_BUILD_CONFIG);
        if (!StringUtil.isEmptyOrSpaces(configValue)) {
            arguments.add("--configuration");
            arguments.add(configValue.trim());
        }

        final String outputValue = parameters.get(DnuConstants.DNU_PARAM_BUILD_OUTPUT);
        if (!StringUtil.isEmptyOrSpaces(outputValue)) {
            arguments.add("--out");
            arguments.add(outputValue.trim());
        }

        final String argumentsValue = parameters.get(DnuConstants.DNU_PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.add(argumentsValue.trim());
        }

        return arguments;
    }
}
