package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version

interface VersionEnumerator {
    fun <T: Versioned>enumerate(versioned: Sequence<T>): Sequence<Pair<String, T>>
}