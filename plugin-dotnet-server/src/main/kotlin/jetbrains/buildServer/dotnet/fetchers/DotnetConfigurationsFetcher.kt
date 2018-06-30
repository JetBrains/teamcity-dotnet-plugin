/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.discovery.SolutionDiscover
import jetbrains.buildServer.dotnet.discovery.StreamFactory
import jetbrains.buildServer.dotnet.discovery.StreamFactoryImpl
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser

/**
 * Provides configurations fetcher for project model.
 */
class DotnetConfigurationsFetcher(private val _solutionDiscover: SolutionDiscover) : ProjectDataFetcher {

    override fun retrieveData(fsBrowser: Browser, projectFilePath: String): MutableList<DataItem> =
            getValues(StreamFactoryImpl(fsBrowser), StringUtil.splitCommandArgumentsAndUnquote(projectFilePath).asSequence())
                    .map { DataItem(it, null) }
                    .toMutableList()

    private fun getValues(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<String> =
            _solutionDiscover.discover(streamFactory, paths)
                    .flatMap { it.projects.asSequence() }
                    .flatMap { it.configurations.asSequence() }
                    .map { it.name }
                    .plus(DefaultConfigurations)
                    .distinctBy { it.toLowerCase() }
                    .sorted()

    override fun getType(): String {
        return "DotnetConfigurations"
    }

    companion object {
        private val DefaultConfigurations: Collection<String> = listOf("Release", "Debug")
    }
}
