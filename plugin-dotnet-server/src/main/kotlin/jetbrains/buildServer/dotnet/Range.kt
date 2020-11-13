package jetbrains.buildServer.dotnet

interface Range<T> {
    fun contains(value: T): Boolean
}