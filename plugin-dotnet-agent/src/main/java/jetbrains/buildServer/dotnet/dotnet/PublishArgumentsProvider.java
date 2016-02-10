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
 * Provides arguments to dotnet publish command.
 */
public class PublishArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DotnetConstants.COMMAND_PUBLISH);

        final String projectsValue = parameters.get(DotnetConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.add(projectsValue.trim());
        }

        final String frameworkValue = parameters.get(DotnetConstants.PARAM_PUBLISH_FRAMEWORK);
        if (!StringUtil.isEmptyOrSpaces(frameworkValue)) {
            arguments.add("--framework");
            arguments.add(frameworkValue.trim());
        }

        final String configValue = parameters.get(DotnetConstants.PARAM_PUBLISH_CONFIG);
        if (!StringUtil.isEmptyOrSpaces(configValue)) {
            arguments.add("--configuration");
            arguments.add(configValue.trim());
        }

        final String runtimeValue = parameters.get(DotnetConstants.PARAM_PUBLISH_RUNTIME);
        if (!StringUtil.isEmptyOrSpaces(runtimeValue)) {
            arguments.add("--runtime");
            arguments.add(runtimeValue.trim());
        }

        final String outputValue = parameters.get(DotnetConstants.PARAM_PUBLISH_OUTPUT);
        if (!StringUtil.isEmptyOrSpaces(outputValue)) {
            arguments.add("--output");
            arguments.add(outputValue.trim());
        }

        final String nativeValue = parameters.get(DotnetConstants.PARAM_PUBLISH_NATIVE);
        if ("true".equalsIgnoreCase(nativeValue)) {
            arguments.add("--native-subdirectory");
        }

        final String argumentsValue = parameters.get(DotnetConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
