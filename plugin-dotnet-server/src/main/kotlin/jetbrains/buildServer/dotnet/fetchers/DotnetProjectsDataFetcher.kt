/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.models.Project
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser

import java.io.File
import java.util.*

/**
 * Provides an abstract fetcher for project model.
 */
abstract class DotnetProjectsDataFetcher(private val myModelParser: DotnetModelParser) : ProjectDataFetcher {

    override fun retrieveData(browser: Browser, projectFilePath: String): List<DataItem> {
        val items = TreeSet(String.CASE_INSENSITIVE_ORDER)
        val projectsPaths = StringUtil.splitCommandArgumentsAndUnquote(projectFilePath)
        if (projectsPaths.size == 0) {
            projectsPaths.add(StringUtil.EMPTY)
        }

        for (projectPath in projectsPaths) {
            val projectFile: String
            if (projectPath.isNullOrBlank()) {
                projectFile = DotnetConstants.PROJECT_JSON
            } else if (FileUtil.getExtension(projectPath).isNullOrBlank()) {
                projectFile = File(projectPath, DotnetConstants.PROJECT_JSON).path
            } else {
                projectFile = projectPath
            }

            val project = myModelParser.getProjectModel(browser.getElement(projectFile)) ?: continue

            items.addAll(getDataItems(project))
        }

        if (items.isEmpty()) {
            return emptyList()
        }

        val data = ArrayList(items)
        Collections.sort(data)

        return data.map { it -> DataItem(it, null) }
    }

    protected abstract fun getDataItems(project: Project?): Collection<String>
}
