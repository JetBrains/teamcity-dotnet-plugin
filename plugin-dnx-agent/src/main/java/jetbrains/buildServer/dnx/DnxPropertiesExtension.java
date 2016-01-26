/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildAgent;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.util.EventDispatcher;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Provides a list of available DNX runtimes.
 */
public class DnxPropertiesExtension extends AgentLifeCycleAdapter {

    private static final Logger LOG = Logger.getInstance(DnxRuntimeDetector.class.getName());
    private final DnxRuntimeDetector runtimeDetector;

    public DnxPropertiesExtension(@NotNull final EventDispatcher<AgentLifeCycleListener> events,
                                  @NotNull final DnxRuntimeDetector runtimeDetector) {
        this.runtimeDetector = runtimeDetector;
        events.addListener(this);
    }

    @Override
    public void beforeAgentConfigurationLoaded(@NotNull BuildAgent agent) {
        final BuildAgentConfiguration config = agent.getConfiguration();

        LOG.info("Observing DNX runtimes");
        final Map<String, String> runtimes = runtimeDetector.getRuntimes();
        for (String name : runtimes.keySet()) {
            LOG.info(String.format("Found DNX runtime %s at %s", name, runtimes.get(name)));
            config.addConfigurationParameter(name, runtimes.get(name));
        }
    }
}