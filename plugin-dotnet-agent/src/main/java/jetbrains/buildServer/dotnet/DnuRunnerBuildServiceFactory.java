/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.runner.CommandLineBuildService;
import jetbrains.buildServer.agent.runner.CommandLineBuildServiceFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Dnu runner service factory.
 */
public class DnuRunnerBuildServiceFactory implements CommandLineBuildServiceFactory {

    @NotNull
    @Override
    public CommandLineBuildService createService() {
        return new DnuRunnerBuildService();
    }

    @NotNull
    @Override
    public AgentBuildRunnerInfo getBuildRunnerInfo() {
        return new AgentBuildRunnerInfo() {
            @NotNull
            @Override
            public String getType() {
                return DnuConstants.RUNNER_TYPE;
            }

            @Override
            public boolean canRun(@NotNull final BuildAgentConfiguration config) {
                return true;
            }
        };
    }
}
