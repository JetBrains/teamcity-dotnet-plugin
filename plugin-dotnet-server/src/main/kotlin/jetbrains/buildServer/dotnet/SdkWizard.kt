package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.discovery.Project
import jetbrains.buildServer.dotnet.discovery.SdkVersion

interface SdkWizard {
    fun suggestSdks(projects: Sequence<Project>, compactMode: Boolean): Sequence<SdkVersion>
}