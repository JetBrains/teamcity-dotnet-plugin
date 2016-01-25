/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import jetbrains.buildServer.dnx.models.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * Provides commands fetcher for dnx project model.
 */
public class DnxCommandsFetcher extends DnxProjectsDataFetcher {
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
