/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers;

import jetbrains.buildServer.dotnet.DnxModelParser;
import jetbrains.buildServer.dotnet.models.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides commands fetcher for dnx project model.
 */
public class DnxCommandsFetcher extends DnxProjectsDataFetcher {

    public DnxCommandsFetcher(@NotNull DnxModelParser modelParser) {
        super(modelParser);
    }

    @NotNull
    @Override
    protected Collection<String> getDataItems(@Nullable final Project project) {
        if (project == null || project.commands == null){
            return Collections.emptySet();
        }

        return project.commands.keySet();
    }

    @NotNull
    @Override
    public String getType() {
        return "DnxCommands";
    }
}
