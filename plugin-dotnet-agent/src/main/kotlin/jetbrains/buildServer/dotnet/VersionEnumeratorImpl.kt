package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Version

class VersionEnumeratorImpl : VersionEnumerator {
    override fun <T: Versioned>enumerate(versioned: Sequence<T>): Sequence<Pair<String, T>> = sequence {
        versioned
                .filter { it.version != Version.Empty }
                .groupBy { Version(it.version.major, it.version.minor) }
                .forEach { (version, group) ->
                    val maxVersion = group.maxByOrNull { it.version }!!
                    yield("${version.major}.${version.minor}" to maxVersion)
                    yieldAll(group.map { it.version.toString() to it })
                }
    }.distinctBy { it.first }
}