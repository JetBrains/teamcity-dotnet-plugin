package jetbrains.buildServer.dotnet.discovery

interface SolutionDiscover {
    fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<Solution>
}