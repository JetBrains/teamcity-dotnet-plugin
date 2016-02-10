/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers;

import jetbrains.buildServer.dotnet.DnxRuntimeDetector;
import jetbrains.buildServer.serverSide.DataItem;
import jetbrains.buildServer.serverSide.ProjectDataFetcher;
import jetbrains.buildServer.util.browser.Browser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides runtimes fetcher for the current user.
 */
public class DnxRuntimeFetcher implements ProjectDataFetcher {

    private final DnxRuntimeDetector myRuntimeDetector;

    public DnxRuntimeFetcher(@NotNull final DnxRuntimeDetector runtimeDetector) {
        myRuntimeDetector = runtimeDetector;
    }

    @NotNull
    @Override
    public List<DataItem> retrieveData(@NotNull Browser fsBrowser, @NotNull String projectFilePath) {
        final List<String> runtimes = new ArrayList<String>(myRuntimeDetector.getRuntimes().keySet());
        runtimes.add("active");
        Collections.sort(runtimes);

        final List<DataItem> items = new ArrayList<DataItem>(runtimes.size());
        for (String runtime : runtimes) {
            items.add(new DataItem(runtime, null));
        }

        return items;
    }

    @NotNull
    @Override
    public String getType() {
        return "DnxRuntime";
    }
}
