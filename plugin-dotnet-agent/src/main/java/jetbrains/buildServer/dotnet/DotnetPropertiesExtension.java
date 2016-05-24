/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a list of available dotnet cli runtimes.
 */
public class DotnetPropertiesExtension extends AgentLifeCycleAdapter {

    private static final Logger LOG = Logger.getInstance(DotnetPropertiesExtension.class.getName());
    private final DotnetToolProvider myToolProvider;

    public DotnetPropertiesExtension(@NotNull final EventDispatcher<AgentLifeCycleListener> events,
                                     @NotNull final DotnetToolProvider toolProvider) {
        myToolProvider = toolProvider;
        events.addListener(this);
    }

    @Override
    public void beforeAgentConfigurationLoaded(@NotNull BuildAgent agent) {
        final BuildAgentConfiguration config = agent.getConfiguration();
        final String toolPath;

        LOG.info("Locating .NET CLI tools");
        try {
            toolPath = myToolProvider.getPath(DotnetConstants.RUNNER_TYPE);
        } catch (ToolCannotBeFoundException e) {
            LOG.debug(e);
            return;
        }

        LOG.info("Found .NET CLI at " + toolPath);
        config.addConfigurationParameter(DotnetConstants.CONFIG_PATH, toolPath);
    }
}