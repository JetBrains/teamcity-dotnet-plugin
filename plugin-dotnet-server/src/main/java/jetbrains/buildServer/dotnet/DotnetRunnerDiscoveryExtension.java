/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.dotnet.models.Project;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.browser.Browser;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Performs .net core projects discovery for .NET CLI tools.
 */
public class DotnetRunnerDiscoveryExtension extends DotnetDiscoveryExtensionBase {

    public DotnetRunnerDiscoveryExtension(@NotNull final DnxModelParser modelParser) {
        super(modelParser);
    }

    @Nullable
    protected DiscoveredObject discover(final Project project, final String fullName) {
        if (!StringUtil.isEmpty(project.testRunner)) {
            return new DiscoveredObject(DotnetConstants.RUNNER_TYPE, CollectionsUtil.asMap(
                    DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_TEST,
                    DotnetConstants.PARAM_PATHS, fullName));
        }

        return new DiscoveredObject(DotnetConstants.RUNNER_TYPE, CollectionsUtil.asMap(
                DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_BUILD,
                DotnetConstants.PARAM_PATHS, fullName));
    }

    @NotNull
    @Override
    protected List<DiscoveredObject> postProcessDiscoveredObjects(@NotNull BuildTypeSettings settings,
                                                                  @NotNull Browser browser,
                                                                  @NotNull List<DiscoveredObject> discovered) {
        if (discovered.size() == 0) {
            return discovered;
        }

        // Order steps
        Collections.sort(discovered, new Comparator<DiscoveredObject>() {
            @Override
            public int compare(DiscoveredObject o1, DiscoveredObject o2) {
                return o1.getType().compareTo(o2.getType()) * -1;
            }
        });

        // Restore nuget packages
        discovered.add(0, new DiscoveredObject(DotnetConstants.RUNNER_TYPE, CollectionsUtil.asMap(
                DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_RESTORE)));

        return discovered;
    }
}
