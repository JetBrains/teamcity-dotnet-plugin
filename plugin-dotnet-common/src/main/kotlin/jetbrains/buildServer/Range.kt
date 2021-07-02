package jetbrains.buildServer

interface Range<T> {
    fun contains(value: T): Boolean
}