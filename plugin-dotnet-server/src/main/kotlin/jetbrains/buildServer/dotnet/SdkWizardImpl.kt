package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.discovery.*
import jetbrains.buildServer.dotnet.fetchers.DotnetSdkFetcher
import jetbrains.buildServer.serverSide.DataItem

class SdkWizardImpl(
        private val _sdkResolver: SdkResolver)
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
            .sortedWith(compareBy({it.versionType}, {-(it.sdkType.order)}, {it.version}))
            .distinctBy { it.version }

    private data class SdkData(val framework: Framework, val properties: Collection<Property>)
}