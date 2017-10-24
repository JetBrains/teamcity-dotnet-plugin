package jetbrains.buildServer.dotnet.discovery

interface SolutionDeserializer {
    fun accept(path: String): Boolean

    fun deserialize(path: String, streamFactory: StreamFactory): Solution
}