/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.dotnet.models.Project
import java.util.TreeSet

/**
 * Provides configurations fetcher for project model.
 */
class DotnetConfigurationsFetcher(modelParser: DotnetModelParser) : DotnetProjectsDataFetcher(modelParser) {
    private val DefaultConfigurations: Collection<String> = listOf("Release", "Debug")
    private val ConditionPattern: Regex = Regex("'\\$\\(Configuration\\)([^']*)' == '([^|]*)([^']*)'")

    override fun getDataItems(project: Project?): Collection<String> {
        val configurations = TreeSet(String.CASE_INSENSITIVE_ORDER)
        configurations.addAll(DefaultConfigurations)
        configurations.addAll(project?.configurations?.keys ?: emptySet())

        return configurations
    }

    public override fun getDataItems(project: CsProject?): Collection<String> {
        val configurations = TreeSet(String.CASE_INSENSITIVE_ORDER)
        configurations.addAll(DefaultConfigurations)

        project?.let {
            val conditions = TreeSet(String.CASE_INSENSITIVE_ORDER)
            it.propertyGroups?.let {
                conditions.addAll(it.map { it.condition }.filterNotNull())
            }

            it.itemGroups?.let {
                conditions.addAll(it.map { it.condition }.filterNotNull())
            }

            conditions.forEach {
                ConditionPattern.find(it)?.let {
                    configurations.add(it.groupValues[2])
                }
            }
        }

        return configurations
    }

    override fun getType(): String {
        return "DotnetConfigurations"
    }
}
