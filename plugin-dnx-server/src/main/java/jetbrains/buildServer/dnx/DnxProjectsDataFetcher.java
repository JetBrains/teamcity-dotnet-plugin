/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jetbrains.buildServer.dnx.models.Project;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.DataItem;
import jetbrains.buildServer.serverSide.ProjectDataFetcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.browser.Browser;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Provides an abstract fetcher for dnx project model.
 */
public abstract class DnxProjectsDataFetcher implements ProjectDataFetcher {

    @NotNull
    @Override
    public List<DataItem> retrieveData(@NotNull Browser browser, @NotNull String projectFilePath) {
        final Set<String> items = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final List<String> projectsPaths = StringUtil.splitCommandArgumentsAndUnquote(projectFilePath);
        if (projectsPaths.size() == 0) {
            projectsPaths.add(StringUtil.EMPTY);
        }

        final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.create();

        for (String projectPath : projectsPaths) {
            final String projectFile = StringUtil.isEmptyOrSpaces(projectPath) ? "project.json" : projectPath.trim();
            InputStream inputStream = null;

            try {
                final Element projectElement = browser.getElement(projectFile);
                if (projectElement == null || !projectElement.isContentAvailable()) {
                    continue;
                }

                inputStream = projectElement.getInputStream();
                final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                final Project project = gson.fromJson(reader, Project.class);

                items.addAll(getDataItems(project));
            } catch (Exception e) {
                String message = "Failed to retrieve file for given path " + projectFile + ": " + e.toString();
                Loggers.SERVER.infoAndDebugDetails(message, e);
            } finally {
                FileUtil.close(inputStream);
            }
        }

        if (items.size() == 0) {
            return Collections.emptyList();
        }

        final ArrayList<DataItem> dataItems = new ArrayList<DataItem>(items.size());
        for (String item : items) {
            dataItems.add(new DataItem(item, null));
        }

        return dataItems;
    }

    @NotNull
    protected abstract Collection<String> getDataItems(@Nullable final Project project);
}
