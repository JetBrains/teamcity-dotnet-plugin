package jetbrains.buildServer.agent.runner

interface Source<T> {
    fun read(source: String, fromPosition: Long, count: Long): Sequence<T>
}