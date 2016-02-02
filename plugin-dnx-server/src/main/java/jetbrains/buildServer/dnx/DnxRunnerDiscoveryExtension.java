/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import jetbrains.buildServer.dnx.models.Project;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.browser.Browser;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Performs .net core projects discovery.
 */
public class DnxRunnerDiscoveryExtension extends BreadthFirstRunnerDiscoveryExtension {

    private static final Pattern TEST_COMMAND = Pattern.compile(".*(test|xunit|nunit).*");
    private static final Pattern WEB_COMMAND = Pattern.compile(".*(web|kestrel).*");

    private final DnxModelParser myModelParser;

    public DnxRunnerDiscoveryExtension(@NotNull final DnxModelParser modelParser) {
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
                final DiscoveredObject runner = discover(item);
                if (runner != null) result.add(runner);
            }
        }

        return result;
    }

    @Nullable
    private DiscoveredObject discover(final Element element) {
        final Project project = myModelParser.getProjectModel(element);
        if (project == null) {
            return null;
        }

        String fullName = element.getFullName();
        if (fullName.contains(" ")) {
            fullName = "\"" + fullName + "\"";
        }

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
                        DnuConstants.PARAM_COMMAND, DnuConstants.COMMAND_PACK,
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
