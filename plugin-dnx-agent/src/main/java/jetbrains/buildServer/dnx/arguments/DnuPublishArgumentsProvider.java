/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.arguments;

import jetbrains.buildServer.dnx.DnuConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides arguments to dnu publish command.
 */
public class DnuPublishArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();
        arguments.add(DnuConstants.COMMAND_PUBLISH);

        final String projectsValue = parameters.get(DnuConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(projectsValue));
        }

        final String frameworkValue = parameters.get(DnuConstants.PARAM_PUBLISH_FRAMEWORK);
        if (!StringUtil.isEmptyOrSpaces(frameworkValue)) {
            final List<String> frameworks = StringUtil.splitCommandArgumentsAndUnquote(frameworkValue);
            for (String framework : frameworks) {
                arguments.add("--framework");
                arguments.add(framework);
            }
        }

        final String configValue = parameters.get(DnuConstants.PARAM_PUBLISH_CONFIG);
        if (!StringUtil.isEmptyOrSpaces(configValue)) {
            final List<String> configurations = StringUtil.splitCommandArgumentsAndUnquote(configValue);
            for (String configuration : configurations) {
                arguments.add("--configuration");
                arguments.add(configuration);
            }
        }

        final String runtimeValue = parameters.get(DnuConstants.PARAM_PUBLISH_RUNTIME);
        if (!StringUtil.isEmptyOrSpaces(runtimeValue)) {
            final List<String> runtimes = StringUtil.splitCommandArgumentsAndUnquote(runtimeValue);
            for (String runtime : runtimes) {
                arguments.add("--runtime");
                arguments.add(runtime);
            }
        }

        final String outputValue = parameters.get(DnuConstants.PARAM_PUBLISH_OUTPUT);
        if (!StringUtil.isEmptyOrSpaces(outputValue)) {
            arguments.add("--out");
            arguments.add(outputValue.trim());
        }

        final String nativeValue = parameters.get(DnuConstants.PARAM_PUBLISH_NATIVE);
        if ("true".equalsIgnoreCase(nativeValue)) {
            arguments.add("--native");
        }

        final String compileValue = parameters.get(DnuConstants.PARAM_PUBLISH_COMPILE_SOURCE);
        if ("true".equalsIgnoreCase(compileValue)) {
            arguments.add("--no-source");
        }

        final String symbolsValue = parameters.get(DnuConstants.PARAM_PUBLISH_INCLUDE_SYMBOLS);
        if ("true".equalsIgnoreCase(symbolsValue)) {
            arguments.add("--include-symbols");
        }

        final String argumentsValue = parameters.get(DnuConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        return arguments;
    }
}
