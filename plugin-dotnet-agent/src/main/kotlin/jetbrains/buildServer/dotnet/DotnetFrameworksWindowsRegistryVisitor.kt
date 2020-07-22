package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.WindowsRegistryKey
import jetbrains.buildServer.agent.WindowsRegistryVisitor

interface DotnetFrameworksWindowsRegistryVisitor: WindowsRegistryVisitor {
    val keys: Sequence<WindowsRegistryKey>

    fun getFrameworks(): Sequence<DotnetFramework>
}