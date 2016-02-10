/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Lookups for .NET CLI utilities.
 */
public class DotnetToolProvider implements ToolProvider {

    private static final String TOOL_NAME = "dotnet";
    private static final String TOOL_HOME = "DOTNET_HOME";
    private static final Pattern TOOL_PATTERN = Pattern.compile("^.*" + TOOL_NAME + "(\\.(exe))?$");
    private static final String INVALID_TOOL_DISTRIB = "Invalid tool %s distribution at %s";

    public DotnetToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry) {
        toolProvidersRegistry.registerToolProvider(this);
    }

    @Override
    public boolean supports(@NotNull String toolName) {
        return TOOL_NAME.equalsIgnoreCase(toolName);
    }

    @NotNull
    @Override
    public String getPath(@NotNull String toolName) throws ToolCannotBeFoundException {
        final String dotnetHome = System.getenv(TOOL_HOME);
        if (StringUtil.isEmpty(dotnetHome)) {
            throw new ToolCannotBeFoundException(String.format("Environment variable '%s' not defined.", TOOL_HOME));
        }

        final File directory = new File(dotnetHome, "bin");
        if (!directory.exists()) {
            throw new ToolCannotBeFoundException(String.format(INVALID_TOOL_DISTRIB, toolName, dotnetHome));
        }

        final File[] files = directory.listFiles();
        if (files == null) {
            throw new ToolCannotBeFoundException(String.format(INVALID_TOOL_DISTRIB, toolName, dotnetHome));
        }

        for (File file : files) {
            final String absolutePath = file.getAbsolutePath();
            if (TOOL_PATTERN.matcher(absolutePath).find()) {
                return absolutePath;
            }
        }

        throw new ToolCannotBeFoundException(String.format(INVALID_TOOL_DISTRIB, toolName, dotnetHome));
    }

    @NotNull
    @Override
    public String getPath(@NotNull String toolName,
                          @NotNull AgentRunningBuild build,
                          @NotNull BuildRunnerContext runner) throws ToolCannotBeFoundException {
        return getPath(toolName);
    }
}
