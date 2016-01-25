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
 * Provides arguments to dnu restore command.
 */
public class DnuRestoreArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DnuConstants.DNU_COMMAND_RESTORE);

        final String projectsValue = parameters.get(DnuConstants.DNU_PARAM_RESTORE_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue));
        }

        final String packagesValue = parameters.get(DnuConstants.DNU_PARAM_PACKAGES_PATH);
        if (!StringUtil.isEmptyOrSpaces(packagesValue)) {
            arguments.add("--packages");
            arguments.add(packagesValue.trim());
        }

        final String parallelValue = parameters.get(DnuConstants.DNU_PARAM_PARALLEL);
        if ("true".equalsIgnoreCase(parallelValue)) {
            arguments.add("--parallel");
        }

        final String argumentsValue = parameters.get(DnuConstants.DNU_PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
