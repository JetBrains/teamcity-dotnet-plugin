

package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.SdkTypeResolver
import jetbrains.buildServer.dotnet.SdkWizard
import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.serverSide.DataItem
import jetbrains.buildServer.serverSide.ProjectDataFetcher
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.browser.Browser

/**
 * Provides SDK fetcher for project model.
 */
class DotnetSdkFetcher(
        private val _solutionDiscover: SolutionDiscover,
        private val _sdkWizard: SdkWizard)
    : ProjectDataFetcher {
    override fun retrieveData(fsBrowser: Browser, projectFilePath: String): MutableList<DataItem> =
            getDataItems(StreamFactoryImpl(fsBrowser), StringUtil.splitCommandArgumentsAndUnquote(projectFilePath).asSequence())
                    .toMutableList()

    private fun getDataItems(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<DataItem> =
            _solutionDiscover.discover(streamFactory, paths)
                    .asSequence()
                    .flatMap { it.projects.asSequence() }
                    .let { _sdkWizard.suggestSdks(it, false) }
                    .map { DataItem(it.version.toString(), createDesctiption(it)) }

    private fun createDesctiption(version: SdkVersion): String
    {
        val prefix = if(version.versionType == SdkVersionType.Compatible) "Compatible " else ""
        return "$prefix${version.sdkType.description}"
    }

    override fun getType(): String {
        return "DotnetSdk"
    }
}