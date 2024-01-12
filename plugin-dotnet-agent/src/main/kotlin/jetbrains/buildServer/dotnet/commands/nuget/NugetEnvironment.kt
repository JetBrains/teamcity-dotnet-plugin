

package jetbrains.buildServer.dotnet.commands.nuget

import jetbrains.buildServer.agent.BuildRunnerSettings

interface NugetEnvironment {
    val allowInternalCaches: Boolean
}