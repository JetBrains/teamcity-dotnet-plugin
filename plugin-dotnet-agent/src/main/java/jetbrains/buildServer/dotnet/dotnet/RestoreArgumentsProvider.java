/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.dotnet;

import jetbrains.buildServer.dotnet.ArgumentsProvider;
import jetbrains.buildServer.dotnet.DotnetConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides arguments to dotnet restore command.
 */
public class RestoreArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DotnetConstants.COMMAND_RESTORE);

        final String projectsValue = parameters.get(DotnetConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue));
        }

        final String packagesValue = parameters.get(DotnetConstants.PARAM_RESTORE_PACKAGES);
        if (!StringUtil.isEmptyOrSpaces(packagesValue)) {
            arguments.add("--packages");
            arguments.add(packagesValue.trim());
        }

        final String sourceValue = parameters.get(DotnetConstants.PARAM_RESTORE_SOURCE);
        if (!StringUtil.isEmptyOrSpaces(sourceValue)) {
            arguments.add("--source");
            arguments.add(sourceValue.trim());
        }

        final String parallelValue = parameters.get(DotnetConstants.PARAM_RESTORE_PARALLEL);
        if ("true".equalsIgnoreCase(parallelValue)) {
            arguments.add("--disable-parallel");
        }

        final String verbosityValue = parameters.get(DotnetConstants.PARAM_VERBOSITY);
        if (!StringUtil.isEmptyOrSpaces(verbosityValue)) {
            arguments.add("--verbosity");
            arguments.add(verbosityValue.trim());
        }

        final String argumentsValue = parameters.get(DotnetConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
