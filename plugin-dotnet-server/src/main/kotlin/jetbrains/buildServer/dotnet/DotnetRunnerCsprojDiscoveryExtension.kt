/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

    private val TestPackages: Regex = Regex("xunit|nunit|test")

    override fun discoverRunnersInDirectory(dir: Element,
                                            filesAndDirs: List<Element>): List<DiscoveredObject> {
        val result = ArrayList<DiscoveredObject>()
        for (item in filesAndDirs) {
            if (!item.isLeaf || !item.isContentAvailable) continue

            if (item.name.endsWith(DotnetConstants.PROJECT_CSPROJ)) {
                val project = myModelParser.getCsProjectModel(item) ?: continue
                val fullName = getEscapedPath(item.fullName)
                val steps = discover(project, fullName)
                result.addAll(steps.map { DiscoveredObject(DotnetConstants.RUNNER_TYPE, it) })
            }

            if (item.name.endsWith(DotnetConstants.PROJECT_SLN)) {
                val projects = myModelParser.getCsProjectModels(item) ?: continue
                val fullName = getEscapedPath(item.fullName)
                val steps = discover(projects, fullName)
                result.addAll(steps.map { DiscoveredObject(DotnetConstants.RUNNER_TYPE, it) })
            }
        }

        return result
    }

    private fun getEscapedPath(name: String): String {
        if (name.contains(" ")) {
            return "\"$name\""
        }
        return name
    }

    /**
     * Discover steps for standalone project
     */
    private fun discover(project: CsProject, fullName: String): List<Map<String, String>> {
        val steps = arrayListOf<Map<String, String>>()
        project.itemGroups?.let {
            val packages = it.fold(hashSetOf<String>(), {
                all, current ->
                current.packageReferences?.let {
                    all.addAll(it.map { it.include }.filterNotNull())
                }
                all
            })

            steps.addAll(restorePackages(fullName, packages))
            steps.addAll(getBuildSteps(fullName, packages))
        }

        return steps
    }

    /**
     * Discover steps for projects in solution.
     */
    private fun discover(projects: List<CsProject>, fullName: String): List<Map<String, String>> {
        val steps = arrayListOf<Map<String, String>>()
        val packages = hashSetOf<String>()

        for (project in projects) {
            project.itemGroups?.let {
                val projectPackages = it.filterNotNull()
                        .map { it.packageReferences }
                        .filterNotNull()
                        .flatMap {
                            it.map { it.include }.filterNotNull()
                        }.toSet()

                val projectPath = getEscapedPath(project.path!!)
                steps.addAll(getBuildSteps(projectPath, projectPackages))
                packages.addAll(projectPackages)
            }
        }

        Collections.sort(steps, { s1, s2 ->
            val command1 = s1[DotnetConstants.PARAM_COMMAND]!!
            val command2 = s2[DotnetConstants.PARAM_COMMAND]!!
            command2.compareTo(command1)
        })

        steps.addAll(0, restorePackages(fullName, packages))

        return steps
    }

    private fun getBuildSteps(fullName: String, packages: Set<String>): List<Map<String, String>> {
        val steps = arrayListOf<Map<String, String>>()

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

        // If unable to determine project type just build it
        if (packages.isNotEmpty() && steps.size == 0) {
            steps.add(mapOf(
                    Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_BUILD),
                    Pair(DotnetConstants.PARAM_PATHS, fullName)))
        }

        return steps
    }

    private fun restorePackages(fullName: String, packages: Set<String>): List<Map<String, String>> {
        if (packages.isEmpty()) return emptyList()

        return listOf(mapOf(
                Pair(DotnetConstants.PARAM_COMMAND, DotnetConstants.COMMAND_RESTORE),
                Pair(DotnetConstants.PARAM_PATHS, fullName)))
    }
}
