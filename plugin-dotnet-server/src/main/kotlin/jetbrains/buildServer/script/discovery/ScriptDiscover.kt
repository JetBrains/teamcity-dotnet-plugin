package jetbrains.buildServer.script.discovery

import jetbrains.buildServer.dotnet.discovery.Solution
import jetbrains.buildServer.dotnet.discovery.StreamFactory

interface ScriptDiscover {
    fun discover(streamFactory: StreamFactory, paths: Sequence<String>): Sequence<Script>
}