/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.models.Project
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element

import java.util.ArrayList

/**
 * Performs .net core projects discovery.
 */
abstract class DotnetDiscoveryExtensionBase(private val myModelParser: DotnetModelParser) : BreadthFirstRunnerDiscoveryExtension(3) {

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

    protected abstract fun discover(project: Project, fullName: String): DiscoveredObject?
}
