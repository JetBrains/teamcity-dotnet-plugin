package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.TargetType

interface SharedCompilation {
    fun requireSuppressing(sdkVersion: Version): Boolean
}