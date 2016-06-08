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
import java.util.List;
import java.util.regex.Pattern;

/**
 * Lookups for .NET CLI utilities.
 */
public class DotnetToolProvider implements ToolProvider {

    private static final Pattern TOOL_PATTERN = Pattern.compile("^.*" + DotnetConstants.RUNNER_TYPE + "(\\.(exe))?$");
    private static final String PATH_VARIABLE = "PATH";
    private static final String UNABLE_TO_LOCATE_TOOL = "Unable to locate tool %s in the system. " +
            "Please make sure that `%s` variable contains .NET CLI toolchain directory or " +
            "defined `%s` variable.";

    public DotnetToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry) {
        toolProvidersRegistry.registerToolProvider(this);
    }

    @Override
    public boolean supports(@NotNull String toolName) {
        return DotnetConstants.RUNNER_TYPE.equalsIgnoreCase(toolName);
    }

    @NotNull
    @Override
    public String getPath(@NotNull String toolName) throws ToolCannotBeFoundException {
        final String pathVariable = System.getenv(PATH_VARIABLE);
        final List<String> paths = StringUtil.splitHonorQuotes(pathVariable, File.pathSeparatorChar);

        // Try to use DOTNET_HOME variable
        final String dotnetHomeVariable = System.getenv(DotnetConstants.TOOL_HOME);
        if (!StringUtil.isEmpty(dotnetHomeVariable)) {
            paths.add(0, dotnetHomeVariable);
        }

        final String toolPath = FileUtils.findToolPath(paths, TOOL_PATTERN);
        if (StringUtil.isEmpty(toolPath)) {
            final String message = String.format(UNABLE_TO_LOCATE_TOOL, toolName, PATH_VARIABLE, DotnetConstants.TOOL_HOME);
            throw new ToolCannotBeFoundException(message);
        }

        return toolPath;
    }

    @NotNull
    @Override
    public String getPath(@NotNull String toolName,
                          @NotNull AgentRunningBuild build,
                          @NotNull BuildRunnerContext runner) throws ToolCannotBeFoundException {
        return getPath(toolName);
    }
}
