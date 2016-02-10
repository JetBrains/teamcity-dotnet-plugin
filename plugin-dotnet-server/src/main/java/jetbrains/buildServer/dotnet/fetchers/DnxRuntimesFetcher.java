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
 * Provides runtimes fetcher for dnx project model.
 */
public class DnxRuntimesFetcher extends DnxProjectsDataFetcher {

    public DnxRuntimesFetcher(@NotNull DnxModelParser modelParser) {
        super(modelParser);
    }

    @NotNull
    @Override
    protected Collection<String> getDataItems(@Nullable final Project project) {
        if (project == null || project.runtimes == null) {
            return Collections.emptySet();
        }

        return project.runtimes.keySet();
    }

    @NotNull
    @Override
    public String getType() {
        return "DnxRuntimes";
    }
}
