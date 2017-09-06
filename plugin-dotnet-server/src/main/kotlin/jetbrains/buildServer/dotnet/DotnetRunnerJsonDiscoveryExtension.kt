/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.models.Project
import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser
import jetbrains.buildServer.util.browser.Element
import java.util.*

/**
 * Performs .net core projects discovery for .NET CLI tools.
 */
class DotnetRunnerJsonDiscoveryExtension(private val myModelParser: DotnetModelParser) : BreadthFirstRunnerDiscoveryExtension(3) {

    override fun discoverRunnersInDirectory(dir: Element,
                                            filesAndDirs: List<Element>): List<DiscoveredObject> {
        val result = ArrayList<DiscoveredObject>()
        for (item in filesAndDirs) {
            if (item.isLeaf && item.name.endsWith(DotnetConstants.PROJECT_JSON) && item.isContentAvailable) {
                val project = myModelParser.getProjectModel(item) ?: continue

                var fullName = item.fullName
                if (fullName.contains(" ")) {
                    fullName = "\"$fullName\""
                }

                val runner = discover(project, fullName)
                if (runner != null) result.add(runner)
            }
        }

        return result
    }

    private fun discover(project: Project, fullName: String): DiscoveredObject? {
        if (!StringUtil.isEmpty(project.testRunner)) {
            return DiscoveredObject(DotnetConstants.RUNNER_TYPE, mapOf(
                    Pair(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Test.id),
                    Pair(DotnetConstants.PARAM_PATHS, fullName)))
        }

        return DiscoveredObject(DotnetConstants.RUNNER_TYPE, mapOf(
                Pair(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Build.id),
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
                Pair(DotnetConstants.PARAM_COMMAND, DotnetCommandType.Restore.id))))

        return discovered
    }
}
