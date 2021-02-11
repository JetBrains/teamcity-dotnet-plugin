package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.discovery.Framework
import jetbrains.buildServer.dotnet.discovery.Project
import jetbrains.buildServer.dotnet.discovery.Property
import jetbrains.buildServer.dotnet.discovery.SdkResolver
import jetbrains.buildServer.dotnet.fetchers.DotnetSdkFetcher
import jetbrains.buildServer.serverSide.DataItem

class SdkWizardImpl(
        private val _sdkResolver: SdkResolver,
        private val _sdkTypeResolver: SdkTypeResolver)
    : SdkWizard {
    override fun suggestSdks(projects: Sequence<Project>) =
            projects
            .flatMap {
                it.frameworks.asSequence().map {
                    framework ->
                    SdkData(framework, it.properties)
                }
            }
            .mapNotNull { _sdkResolver.resolveSdkVersions(it.framework, it.properties) }
            .flatMap { it }
            .distinct()
            .map { Pair(it, _sdkTypeResolver.tryResolve(it)) }
            .sortedWith(compareBy({-(it.second?.order ?: Int.MAX_VALUE)}, {it.first}))
            .toList()
            .reversed()
            .asSequence()
            .map { it.first }

    private data class SdkData(val framework: Framework, val properties: Collection<Property>)
}