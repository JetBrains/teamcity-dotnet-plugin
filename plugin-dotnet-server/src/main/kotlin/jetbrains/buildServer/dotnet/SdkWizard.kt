package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.discovery.Project

interface SdkWizard {
    fun suggestSdks(projects: Sequence<Project>): Sequence<Version>
}