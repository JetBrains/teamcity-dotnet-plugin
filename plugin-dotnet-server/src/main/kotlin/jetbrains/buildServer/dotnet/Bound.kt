package jetbrains.buildServer.dotnet

data class Bound<T>(val version: T, val include: Boolean = true)
fun <T>T.including() = Bound<T>(this, true)
fun <T>T.excluding() = Bound<T>(this, false)