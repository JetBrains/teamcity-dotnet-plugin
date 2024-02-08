package jetbrains.buildServer.dotnet.requirements

import jetbrains.buildServer.requirements.Requirement

interface SdkBasedRequirementFactory {
    fun tryCreate(sdkVersion: String): Requirement?
}