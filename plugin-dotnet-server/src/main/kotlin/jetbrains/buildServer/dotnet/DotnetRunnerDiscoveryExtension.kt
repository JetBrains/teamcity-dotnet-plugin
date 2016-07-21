/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.models.Project
import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser
import java.util.*

/**
 * Performs .net core projects discovery for .NET CLI tools.
 */
class DotnetRunnerDiscoveryExtension(modelParser: DotnetModelParser) : DotnetDiscoveryExtensionBase(modelParser) {

    override fun discover(project: Project, fullName: String): DiscoveredObject? {
        if (!StringUtil.isEmpty(project.testRunner)) {
            return DiscoveredObject(DotnetConstants.RUNNER_TYPE, mapOf(
                    Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_TEST),
                    Pair(DotnetConstants.PARAM_PATHS, fullName)))
        }

        return DiscoveredObject(DotnetConstants.RUNNER_TYPE, mapOf(
                Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_BUILD),
                Pair(DotnetConstants.PARAM_PATHS, fullName)))
    }

    override fun postProcessDiscoveredObjects(settings: BuildTypeSettings,
                                              browser: Browser,
                                              discovered: MutableList<DiscoveredObject>): List<DiscoveredObject> {
        if (discovered.size == 0) {
            return discovered
        }

        // Order steps
        Collections.sort(discovered) { o1, o2 -> o1.type.compareTo(o2.type) * -1 }

        // Restore nuget packages
        discovered.add(0, DiscoveredObject(DotnetConstants.RUNNER_TYPE, mapOf(
                Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_RESTORE))))

        return discovered
    }
}
