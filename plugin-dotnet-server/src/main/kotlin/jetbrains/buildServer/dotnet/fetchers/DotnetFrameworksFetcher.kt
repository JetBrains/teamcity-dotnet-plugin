/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.dotnet.models.Project

/**
 * Provides frameworks fetcher for project model.
 */
class DotnetFrameworksFetcher(modelParser: DotnetModelParser) : DotnetProjectsDataFetcher(modelParser) {

    override fun getDataItems(project: Project?): Collection<String> {
        return project?.frameworks?.keys ?: emptySet()
    }

    override fun getDataItems(project: CsProject?): Collection<String> {
        project?.let {
            it.PropertyGroups?.let {
                return it.fold(hashSetOf<String>(), {
                    all, current ->
                    current.TargetFramework?.let {
                        all.add(it)
                    }
                    current.TargetFrameworks?.let {
                        all.addAll(it.split(';'))
                    }
                    all
                })
            }
        }

        return emptyList()
    }

    override fun getType(): String {
        return "DotnetFrameworks"
    }
}
