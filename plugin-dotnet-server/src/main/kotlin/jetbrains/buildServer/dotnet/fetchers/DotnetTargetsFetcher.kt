package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser

/**
 * Provides targets fetcher for project model.
 */
class DotnetTargetsFetcher(
        private val _solutionDiscover: SolutionDiscover,
        private val _projectTypeSelector: ProjectTypeSelector)
    : ProjectDataFetcher {

    override fun retrieveData(fsBrowser: Browser, projectFilePath: String): MutableList<DataItem> =
            getValues(StreamFactoryImpl(fsBrowser), StringUtil.splitCommandArgumentsAndUnquote(projectFilePath).asSequence())
                    .map { DataItem(it, null) }
                    .toMutableList()

    fun getValues(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<String> {
        val projects = _solutionDiscover.discover(streamFactory, paths).flatMap { it.projects.asSequence() }.toList()
        val projectTypes = projects.flatMap { _projectTypeSelector.select(it) }.toSet()
        var discoveredTargets = projects.flatMap { it.targets }.map { it.name }

        val targets = mutableListOf<String>()
        if (projectTypes.contains(ProjectType.Test)) {
            targets.addAll(TestTargets)
            discoveredTargets = exclude(discoveredTargets, TestTargets)
        }

        if (projectTypes.contains(ProjectType.Publish)) {
            targets.addAll(PublishTargets)
            discoveredTargets = exclude(discoveredTargets, PublishTargets)
        }

        return InitialDefaultTargets.plus(discoveredTargets).plus(targets).plus(FinishDefaultTargets).asSequence()
                .filter { !it.isNullOrBlank() }
                .distinctBy() { it.toLowerCase() }
    }

    private fun exclude(src: List<String>, items: List<String>): List<String> {
        val curItems = items.map { it.toLowerCase() }.toSet()
        return src.filter { !curItems.contains(it.toLowerCase()) }
    }

    override fun getType(): String {
        return "DotnetTargets"
    }

    private fun getDefaultTargets() {
    }

    companion object {
        val InitialDefaultTargets = listOf("Clean", "Restore", "Rebuild", "Build")
        val FinishDefaultTargets = listOf("Pack")
        val TestTargets = listOf("VSTest")
        val PublishTargets = listOf("Publish")
    }
}