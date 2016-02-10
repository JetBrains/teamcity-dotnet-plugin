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
 * Provides arguments to dotnet build command.
 */
public class BuildArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DotnetConstants.COMMAND_BUILD);

        final String projectsValue = parameters.get(DotnetConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue));
        }

        final String frameworkValue = parameters.get(DotnetConstants.PARAM_BUILD_FRAMEWORK);
        if (!StringUtil.isEmptyOrSpaces(frameworkValue)) {
            final List<String> frameworks = StringUtil.splitCommandArgumentsAndUnquote(frameworkValue);
            for (String framework : frameworks) {
                arguments.add("--framework");
                arguments.add(framework);
            }
        }

        final String configValue = parameters.get(DotnetConstants.PARAM_BUILD_CONFIG);
        if (!StringUtil.isEmptyOrSpaces(configValue)) {
            arguments.add("--configuration");
            arguments.add(configValue.trim());
        }

        final String runtimeValue = parameters.get(DotnetConstants.PARAM_BUILD_RUNTIME);
        if (!StringUtil.isEmptyOrSpaces(runtimeValue)) {
            arguments.add("--runtime");
            arguments.add(runtimeValue.trim());
        }

        final String archValue = parameters.get(DotnetConstants.PARAM_BUILD_ARCH);
        if (!StringUtil.isEmptyOrSpaces(archValue)) {
            arguments.add("--arch");
            arguments.add(archValue.trim());
        }

        final String nativeValue = parameters.get(DotnetConstants.PARAM_BUILD_NATIVE);
        if ("true".equalsIgnoreCase(nativeValue)) {
            arguments.add("--native");
        }

        final String cppValue = parameters.get(DotnetConstants.PARAM_BUILD_CPP);
        if ("true".equalsIgnoreCase(cppValue)) {
            arguments.add("--cpp");
        }

        final String buildProfileValue = parameters.get(DotnetConstants.PARAM_BUILD_PROFILE);
        if ("true".equalsIgnoreCase(buildProfileValue)) {
            arguments.add("--build-profile");
        }

        final String nonIncrementalValue = parameters.get(DotnetConstants.PARAM_BUILD_NON_INCREMENTAL);
        if ("true".equalsIgnoreCase(nonIncrementalValue)) {
            arguments.add("--force-incremental-unsafe");
        }

        final String outputValue = parameters.get(DotnetConstants.PARAM_BUILD_OUTPUT);
        if (!StringUtil.isEmptyOrSpaces(outputValue)) {
            arguments.add("--output");
            arguments.add(outputValue.trim());
        }

        final String tempValue = parameters.get(DotnetConstants.PARAM_BUILD_TEMP);
        if (!StringUtil.isEmptyOrSpaces(tempValue)) {
            arguments.add("--temp-output");
            arguments.add(tempValue.trim());
        }

        final String argumentsValue = parameters.get(DotnetConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
