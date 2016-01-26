/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.fetchers;

import jetbrains.buildServer.dnx.models.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provides configurations fetcher for dnx project model.
 */
public class DnxConfigurationsFetcher extends DnxProjectsDataFetcher {
    @NotNull
    @Override
    protected Collection<String> getDataItems(@Nullable final Project project) {
        final Set<String> configurations = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        configurations.add("Release");
        configurations.add("Debug");

        if (project != null && project.configurations != null) {
            configurations.addAll(project.configurations.keySet());
        }

        return configurations;
    }

    @NotNull
    @Override
    public String getType() {
        return "DnxConfigurations";
    }
}
