package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser

/**
 * Provides targets fetcher for project model.
 */
class DotnetTargetsFetcher(private val _solutionDiscover: SolutionDiscover) : ProjectDataFetcher {

    override fun retrieveData(fsBrowser: Browser, projectFilePath: String): MutableList<DataItem> =
            getValues(StreamFactoryImpl(fsBrowser), StringUtil.splitCommandArgumentsAndUnquote(projectFilePath).asSequence())
                    .map { DataItem(it, null) }
                    .toMutableList()

    fun getValues(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<String> =
            _solutionDiscover.discover(streamFactory, paths)
                    .flatMap { it.projects.asSequence() }
                    .flatMap { it.configurations.asSequence() }
                    .map { it.name }
                    .plus(DefaultTargets)
                    .distinctBy() { it.toLowerCase() }
                    .sorted()

    override fun getType(): String {
        return "DotnetTargets"
    }

    companion object {
        private val DefaultTargets: Collection<String> = listOf("Build", "Clean", "Pack", "Publish", "Rebuild", "Restore")
    }
}