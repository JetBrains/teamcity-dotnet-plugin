

package jetbrains.buildServer.dotnet.discovery

interface SolutionDeserializer {
    fun isAccepted(path: String): Boolean

    fun deserialize(path: String, streamFactory: StreamFactory): Solution
}