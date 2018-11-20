package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.TargetType

interface SharedCompilation {
    fun requireSuppressing(context: DotnetBuildContext): Boolean
}