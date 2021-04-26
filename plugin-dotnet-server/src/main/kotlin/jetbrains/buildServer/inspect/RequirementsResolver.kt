package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.Version
import jetbrains.buildServer.requirements.Requirement

interface RequirementsResolver {
    fun resolve(version: Version, platform: IspectionToolPlatform): Sequence<Requirement>
}