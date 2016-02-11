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
 * Provides arguments to dotnet test command.
 */
public class TestArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DotnetConstants.COMMAND_TEST);

        final String projectsValue = parameters.get(DotnetConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.add(projectsValue.trim());
        }

        final String argumentsValue = parameters.get(DotnetConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
