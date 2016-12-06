/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject
import jetbrains.buildServer.util.browser.Element
import java.util.*

/**
 * Performs .net core projects discovery for .NET CLI tools.
 */
class DotnetRunnerCsprojDiscoveryExtension(private val myModelParser: DotnetModelParser) : BreadthFirstRunnerDiscoveryExtension(3) {

    private val MsBuildVersion: List<String> = listOf("15.0")
    private val TestPackages: Regex = Regex("xunit|nunit|test")

    override fun discoverRunnersInDirectory(dir: Element,
                                            filesAndDirs: List<Element>): List<DiscoveredObject> {
        val result = ArrayList<DiscoveredObject>()
        for (item in filesAndDirs) {
            if (item.isLeaf && item.name.endsWith(DotnetConstants.PROJECT_CSPROJ) && item.isContentAvailable) {
                val project = myModelParser.getCsProjectModel(item) ?: continue

                var fullName = item.fullName
                if (fullName.contains(" ")) {
                    fullName = "\"$fullName\""
                }

                val steps = discover(project, fullName)
                result.addAll(steps.map { DiscoveredObject(DotnetConstants.RUNNER_TYPE, it) })
            }
        }

        return result
    }

    private fun discover(project: CsProject, fullName: String): ArrayList<Map<String, String>> {
        if (project.ToolsVersion.isNullOrEmpty() || !MsBuildVersion.contains(project.ToolsVersion)) {
            return arrayListOf()
        }

        val steps = arrayListOf<Map<String, String>>()
        project.ItemGroups?.let {
            val packages = it.fold(hashSetOf<String>(), {
                all, current ->
                current.PackageReferences?.let {
                    all.addAll(it.map { it.Include }.filterNotNull())
                }
                all
            })

            // Restore nuget packages
            if (packages.size > 0) {
                steps.add(mapOf(
                        Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_RESTORE),
                        Pair(DotnetConstants.PARAM_PATHS, fullName)))
            }

            // Check whether project contains test framework packages
            if (packages.any { TestPackages.matches(it) }) {
                steps.add(mapOf(
                        Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_TEST),
                        Pair(DotnetConstants.PARAM_PATHS, fullName)))
            }

            // Check whether project contains web application packages
            if (packages.any { it.startsWith("Microsoft.AspNet") }) {
                steps.add(mapOf(
                        Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_PUBLISH),
                        Pair(DotnetConstants.PARAM_PATHS, fullName)))
            }
        }

        if (steps.size == 0) {
            steps.add(mapOf(
                    Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_BUILD),
                    Pair(DotnetConstants.PARAM_PATHS, fullName)))
        }

        return steps
    }
}
