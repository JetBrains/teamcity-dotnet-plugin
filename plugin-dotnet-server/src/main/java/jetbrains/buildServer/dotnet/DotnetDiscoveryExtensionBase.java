/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.dotnet.models.Project;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs .net core projects discovery.
 */
public abstract class DotnetDiscoveryExtensionBase extends BreadthFirstRunnerDiscoveryExtension {
    @NotNull
    private final DnxModelParser myModelParser;

    public DotnetDiscoveryExtensionBase(@NotNull final DnxModelParser modelParser) {
        super(3);
        myModelParser = modelParser;
    }

    @NotNull
    @Override
    protected List<DiscoveredObject> discoverRunnersInDirectory(@NotNull final Element dir,
                                                                @NotNull final List<Element> filesAndDirs) {
        final List<DiscoveredObject> result = new ArrayList<DiscoveredObject>();
        for (Element item : filesAndDirs) {
            if (item.isLeaf() && item.getName().endsWith(DnxConstants.PROJECT_JSON) && item.isContentAvailable()) {
                final Project project = myModelParser.getProjectModel(item);
                if (project == null) {
                    continue;
                }

                String fullName = item.getFullName();
                if (fullName.contains(" ")) {
                    fullName = "\"" + fullName + "\"";
                }

                final DiscoveredObject runner = discover(project, fullName);
                if (runner != null) result.add(runner);
            }
        }

        return result;
    }

    protected abstract DiscoveredObject discover(final Project project, final String fullName);
}
