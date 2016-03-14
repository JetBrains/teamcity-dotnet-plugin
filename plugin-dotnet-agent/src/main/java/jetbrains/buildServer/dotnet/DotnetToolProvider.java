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

    private static final String TOOL_NAME = "dotnet";
    private static final Pattern TOOL_PATTERN = Pattern.compile("^.*" + TOOL_NAME + "(\\.(exe))?$");

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
        final String pathVariable = System.getenv("PATH");
        final List<String> paths = StringUtil.splitHonorQuotes(pathVariable, File.pathSeparatorChar);

        // Try to use DOTNET_HOME variable
        final String dotnetHomeVariable = System.getenv(DotnetConstants.TOOL_HOME);
        if (!StringUtil.isEmpty(dotnetHomeVariable)) {
            final File binDirectory = new File(dotnetHomeVariable, "bin");
            paths.add(0, binDirectory.getAbsolutePath());
        }

        final String toolPath = FileUtils.findToolPath(paths, TOOL_PATTERN);
        if (StringUtil.isEmpty(toolPath)){
            throw new ToolCannotBeFoundException(String.format("Unable to locate tool %s in system", toolName));
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
