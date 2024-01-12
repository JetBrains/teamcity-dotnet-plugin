

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.discovery.SolutionDiscover
import jetbrains.buildServer.dotnet.discovery.StreamFactory
import jetbrains.buildServer.dotnet.discovery.StreamFactoryImpl
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser

/**
 * Provides frameworks fetcher for project model.
 */
class DotnetFrameworksFetcher(private val _solutionDiscover: SolutionDiscover) : ProjectDataFetcher {
    override fun retrieveData(fsBrowser: Browser, projectFilePath: String): MutableList<DataItem> =
            getValues(StreamFactoryImpl(fsBrowser), StringUtil.splitCommandArgumentsAndUnquote(projectFilePath).asSequence())
                    .map { DataItem(it, null) }
                    .toMutableList()

    private fun getValues(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<String> =
            _solutionDiscover.discover(streamFactory, paths)
                    .asSequence()
                    .flatMap { it.projects.asSequence() }
                    .flatMap { it.frameworks.asSequence() }
                    .map { it.name }
                    .distinctBy { it.lowercase() }
                    .sorted()

    override fun getType(): String {
        return "DotnetFrameworks"
    }
}