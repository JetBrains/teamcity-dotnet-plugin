package jetbrains.buildServer.dotnet.coverage

interface Sequencer<T> {
    fun nextFrom(value: T): T
}