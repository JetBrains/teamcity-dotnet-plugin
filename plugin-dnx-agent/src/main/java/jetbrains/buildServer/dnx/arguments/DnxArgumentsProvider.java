/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.arguments;

import jetbrains.buildServer.dnx.DnxConstants;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides arguments to dnx command.
 */
public class DnxArgumentsProvider implements ArgumentsProvider {

    @NotNull
    @Override
    public List<String> getArguments(@NotNull final Map<String, String> parameters) {
        final List<String> arguments = new ArrayList<String>();


        final String frameworkValue = parameters.get(DnxConstants.PARAM_FRAMEWORK);
        if (!StringUtil.isEmptyOrSpaces(frameworkValue)) {
            arguments.add("--framework");
            arguments.add(frameworkValue.trim());
        }

        final String configValue = parameters.get(DnxConstants.PARAM_CONFIG);
        if (!StringUtil.isEmptyOrSpaces(configValue)) {
            arguments.add("--configuration");
            arguments.add(configValue);
        }

        final String appbaseValue = parameters.get(DnxConstants.PARAM_APPBASE);
        if (!StringUtil.isEmptyOrSpaces(appbaseValue)) {
            arguments.add("--appbase");
            arguments.add(appbaseValue.trim());
        }

        final String libsValue = parameters.get(DnxConstants.PARAM_LIBS);
        if (!StringUtil.isEmptyOrSpaces(libsValue)) {
            arguments.add("--lib");
            arguments.add(libsValue.trim());
        }

        final String packagesValue = parameters.get(DnxConstants.PARAM_PACKAGES);
        if (!StringUtil.isEmptyOrSpaces(packagesValue)) {
            arguments.add("--packages");
            arguments.add(packagesValue.trim());
        }

        final String argumentsValue = parameters.get(DnxConstants.PARAM_ARGUMENTS);
        if (!StringUtil.isEmptyOrSpaces(argumentsValue)) {
            arguments.addAll(StringUtil.splitCommandArgumentsAndUnquote(argumentsValue));
        }

        final String projectValue = parameters.get(DnxConstants.PARAM_PATHS);
        if (!StringUtil.isEmptyOrSpaces(projectValue)) {
            arguments.add("--project");
            arguments.add(projectValue.trim());
        }

        arguments.add(parameters.get(DnxConstants.PARAM_COMMAND));

        return arguments;
    }
}
