/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.dotnet.models.Project
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
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
        if (projectsPaths.isEmpty()) {
            projectsPaths.add(StringUtil.EMPTY)
        }

        for (projectPath in projectsPaths) {
            val path = projectPath.trimEnd('/')
            val jsonProjectFile: String = getJsonProject(path)
            myModelParser.getProjectModel(browser.getElement(jsonProjectFile))?.let {
                items.addAll(getDataItems(it))
            }

            val csProjectFile: String = getCsProject(path, browser)
            myModelParser.getCsProjectModel(browser.getElement(csProjectFile))?.let {
                items.addAll(getDataItems(it))
            }
        }

        if (items.isEmpty()) {
            return emptyList()
        }

        val data = ArrayList(items)
        Collections.sort(data)

        return data.map { it -> DataItem(it, null) }
    }

    private fun getJsonProject(projectPath: String): String {
        val projectFile: String
        if (projectPath.isNullOrBlank()) {
            projectFile = DotnetConstants.PROJECT_JSON
        } else if (!projectPath.endsWith(DotnetConstants.PROJECT_JSON, ignoreCase = true)) {
            projectFile = File(projectPath, DotnetConstants.PROJECT_JSON).path
        } else {
            projectFile = projectPath
        }
        return projectFile
    }

    private fun getCsProject(projectPath: String, browser: Browser): String {
        if (!projectPath.endsWith(DotnetConstants.PROJECT_CSPROJ, ignoreCase = true)) {
            browser.getElement(projectPath)?.let {
                it.children?.let {
                    it.firstOrNull {
                        it.fullName.endsWith(DotnetConstants.PROJECT_CSPROJ, ignoreCase = true)
                    }?.let {
                        return it.fullName
                    }
                }
            }
        }

        return projectPath
    }

    protected abstract fun getDataItems(project: Project?): Collection<String>

    protected abstract fun getDataItems(project: CsProject?): Collection<String>
}
