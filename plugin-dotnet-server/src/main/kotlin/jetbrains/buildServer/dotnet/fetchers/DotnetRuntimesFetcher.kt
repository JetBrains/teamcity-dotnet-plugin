/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.models.Project

/**
 * Provides runtimes fetcher for project model.
 */
class DotnetRuntimesFetcher(modelParser: DotnetModelParser) : DotnetProjectsDataFetcher(modelParser) {

    override fun getDataItems(project: Project?): Collection<String> {
        return project?.runtimes?.keys ?: emptySet()
    }

    override fun getType(): String {
        return "DotnetRuntimes"
    }
}
