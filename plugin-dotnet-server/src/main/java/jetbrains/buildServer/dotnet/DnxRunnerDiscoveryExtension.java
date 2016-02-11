/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.dotnet.models.Project;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.browser.Browser;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Performs .net core projects discovery for DNX tools.
 */
public class DnxRunnerDiscoveryExtension extends DotnetDiscoveryExtensionBase {

    private static final Pattern TEST_COMMAND = Pattern.compile(".*(test|xunit|nunit).*");
    private static final Pattern WEB_COMMAND = Pattern.compile(".*(web|kestrel).*");

    public DnxRunnerDiscoveryExtension(@NotNull final DnxModelParser modelParser) {
        super(modelParser);
    }

    @Nullable
    protected DiscoveredObject discover(final Project project, final String fullName) {
        if (project.commands == null || project.commands.size() == 0) {
            return new DiscoveredObject(DnuConstants.RUNNER_TYPE, CollectionsUtil.asMap(
                    DnuConstants.PARAM_COMMAND, DnuConstants.COMMAND_BUILD,
                    DnuConstants.PARAM_PATHS, fullName));
        }

        final Set<String> commands = new TreeSet<String>(project.commands.keySet());
        for (String command : commands) {
            if (TEST_COMMAND.matcher(command).find()) {
                return new DiscoveredObject(DnxConstants.RUNNER_TYPE, CollectionsUtil.asMap(
                        DnxConstants.PARAM_COMMAND, command,
                        DnxConstants.PARAM_PATHS, fullName));
            }
        }

        for (String command : commands) {
            if (WEB_COMMAND.matcher(command).find()) {
                return new DiscoveredObject(DnuConstants.RUNNER_TYPE, CollectionsUtil.asMap(
                        DnuConstants.PARAM_COMMAND, DnuConstants.COMMAND_PUBLISH,
                        DnuConstants.PARAM_PATHS, fullName));
            }
        }

        return null;
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
        discovered.add(0, new DiscoveredObject(DnuConstants.RUNNER_TYPE, CollectionsUtil.asMap(
                DnuConstants.PARAM_COMMAND, DnuConstants.COMMAND_RESTORE)));

        return discovered;
    }
}
