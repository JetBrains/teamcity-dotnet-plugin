package jetbrains.buildServer.dotnet

import jetbrains.buildServer.requirements.Requirement

interface RequirementFactory {
    fun tryCreate(sdkVersion: String): Requirement?
}