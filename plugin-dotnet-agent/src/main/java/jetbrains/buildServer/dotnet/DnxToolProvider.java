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
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Lookups for DNX utilities.
 */
public class DnxToolProvider implements ToolProvider {

    private static final Map<String, Pattern> DNX_TOOLS;
    private static final String PATH_VARIABLE = "PATH";
    private static final String UNABLE_TO_LOCATE_TOOL = "Unable to locate tool %s in the system. " +
            "Please make sure that `%s` variable contains DNX toolkit directory or " +
            "defined `%s` variable.";


    public DnxToolProvider(@NotNull final ToolProvidersRegistry toolProvidersRegistry) {
        toolProvidersRegistry.registerToolProvider(this);
    }

    @Override
    public boolean supports(@NotNull String toolName) {
        return DNX_TOOLS.containsKey(toolName);
    }

    @NotNull
    @Override
    public String getPath(@NotNull String toolName) throws ToolCannotBeFoundException {
        if (!DNX_TOOLS.containsKey(toolName)) {
            throw new ToolCannotBeFoundException(String.format("Tool %s is not supported", toolName));
        }

        final String pathVariable = System.getenv(PATH_VARIABLE);
        final List<String> paths = StringUtil.splitHonorQuotes(pathVariable, File.pathSeparatorChar);

        // Try to use DNX_PATH variable
        final String dnxPathVariable = System.getenv(DnxConstants.DNX_PATH);
        if (!StringUtil.isEmpty(dnxPathVariable)) {
            final String parentDirectory = new File(dnxPathVariable).getParent();
            if (!StringUtil.isEmpty(parentDirectory)) {
                paths.add(0, parentDirectory);
            }
        }

        final String toolPath = FileUtils.findToolPath(paths, DNX_TOOLS.get(toolName));
        if (StringUtil.isEmpty(toolPath)) {
            final String message = String.format(UNABLE_TO_LOCATE_TOOL, toolName, PATH_VARIABLE, DnxConstants.DNX_PATH);
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

    static {
        DNX_TOOLS = new TreeMap<String, Pattern>(String.CASE_INSENSITIVE_ORDER);
        DNX_TOOLS.put("dnu", Pattern.compile("^.*dnu(\\.(cmd|bat))?$"));
        DNX_TOOLS.put("dnx", Pattern.compile("^.*dnx(\\.(exe))?$"));
    }
}
