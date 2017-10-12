package jetbrains.buildServer.dotnet.fetchers

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.dotnet.models.Project

/**
 * Provides targets fetcher for project model.
 */
class DotnetTargetsFetcher(modelParser: DotnetModelParser) : DotnetProjectsDataFetcher(modelParser) {

    override fun getDataItems(project: Project?) = emptyList<String>()

    override fun getDataItems(project: CsProject?) = listOf("Build", "Clean", "Pack", "Publish", "Rebuild", "Restore")

    override fun getType(): String {
        return "DotnetTargets"
    }
}