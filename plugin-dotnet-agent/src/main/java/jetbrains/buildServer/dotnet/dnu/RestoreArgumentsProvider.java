/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.dnu;

import jetbrains.buildServer.dotnet.DnuConstants;
import jetbrains.buildServer.dotnet.ArgumentsProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides arguments to dnu restore command.
 */
public class RestoreArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DnuConstants.COMMAND_RESTORE);

        final String projectsValue = parameters.get(DnuConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue));
        }

        final String packagesValue = parameters.get(DnuConstants.PARAM_RESTORE_PACKAGES);
        if (!StringUtil.isEmptyOrSpaces(packagesValue)) {
            arguments.add("--packages");
            arguments.add(packagesValue.trim());
        }

        final String parallelValue = parameters.get(DnuConstants.PARAM_RESTORE_PARALLEL);
        if ("true".equalsIgnoreCase(parallelValue)) {
            arguments.add("--parallel");
        }

        final String argumentsValue = parameters.get(DnuConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
