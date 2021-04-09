package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.BuildRunnerSettings

interface NugetEnvironment {
    val allowInternalCaches: Boolean
}