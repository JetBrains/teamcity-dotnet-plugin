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
 * Provides arguments to dotnet pack command.
 */
public class PackArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DotnetConstants.COMMAND_PACK);

        final String projectsValue = parameters.get(DotnetConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue));
        }

        final String basePathValue = parameters.get(DotnetConstants.PARAM_PACK_BASE);
        if (!StringUtil.isEmptyOrSpaces(basePathValue)) {
            arguments.add("--basepath");
            arguments.add(basePathValue.trim());
        }

        final String configValue = parameters.get(DotnetConstants.PARAM_PACK_CONFIG);
        if (!StringUtil.isEmptyOrSpaces(configValue)) {
            arguments.add("--configuration");
            arguments.add(configValue.trim());
        }

        final String outputValue = parameters.get(DotnetConstants.PARAM_PACK_OUTPUT);
        if (!StringUtil.isEmptyOrSpaces(outputValue)) {
            arguments.add("--output");
            arguments.add(outputValue.trim());
        }

        final String tempValue = parameters.get(DotnetConstants.PARAM_PACK_TEMP);
        if (!StringUtil.isEmptyOrSpaces(tempValue)) {
            arguments.add("--temp-output");
            arguments.add(tempValue.trim());
        }

        final String versionSuffixValue = parameters.get(DotnetConstants.PARAM_PACK_VERSION_SUFFIX);
        if (!StringUtil.isEmptyOrSpaces(versionSuffixValue)) {
            arguments.add("--version-suffix");
            arguments.add(versionSuffixValue.trim());
        }

        final String argumentsValue = parameters.get(DotnetConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
