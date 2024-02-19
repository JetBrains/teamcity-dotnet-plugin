package jetbrains.buildServer.dotnet.requirements

import jetbrains.buildServer.requirements.Requirement

interface SDKBasedRequirementFactory {
    fun tryCreate(sdkVersion: String): Requirement?
}