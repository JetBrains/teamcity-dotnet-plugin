/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers;

import jetbrains.buildServer.dotnet.DnxModelParser;
import jetbrains.buildServer.dotnet.DotnetConstants;
import jetbrains.buildServer.dotnet.models.Project;
import jetbrains.buildServer.serverSide.DataItem;
import jetbrains.buildServer.serverSide.ProjectDataFetcher;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.browser.Browser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * Provides an abstract fetcher for dnx project model.
 */
public abstract class DnxProjectsDataFetcher implements ProjectDataFetcher {

    private final DnxModelParser myModelParser;

    public DnxProjectsDataFetcher(@NotNull DnxModelParser modelParser) {
        myModelParser = modelParser;
    }

    @NotNull
    @Override
    public List<DataItem> retrieveData(@NotNull Browser browser, @NotNull String projectFilePath) {
        final Set<String> items = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        final List<String> projectsPaths = StringUtil.splitCommandArgumentsAndUnquote(projectFilePath);
        if (projectsPaths.size() == 0) {
            projectsPaths.add(StringUtil.EMPTY);
        }

        for (String projectPath : projectsPaths) {
            final String projectFile;
            if (StringUtil.isEmptyOrSpaces(projectPath)) {
                projectFile = DotnetConstants.PROJECT_JSON;
            } else if (StringUtil.isEmpty(FileUtil.getExtension(projectPath))) {
                projectFile = new File(projectPath, DotnetConstants.PROJECT_JSON).getPath();
            } else {
                projectFile = projectPath;
            }

            final Project project = myModelParser.getProjectModel(browser.getElement(projectFile));
            if (project == null) {
                continue;
            }

            items.addAll(getDataItems(project));
        }

        if (items.size() == 0) {
            return Collections.emptyList();
        }

        final List<String> data = new ArrayList<String>(items);
        Collections.sort(data);

        final List<DataItem> dataItems = new ArrayList<DataItem>(data.size());
        for (String item : data) {
            dataItems.add(new DataItem(item, null));
        }

        return dataItems;
    }

    @NotNull
    protected abstract Collection<String> getDataItems(@Nullable final Project project);
}
