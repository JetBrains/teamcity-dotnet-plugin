package jetbrains.buildServer.agent

import jetbrains.buildServer.dotnet.Platform

enum class WindowsRegistryBitness(val id: String, val platform: Platform) {
    Bitness32("32", Platform.x86),
    Bitness64("64", Platform.x64);
}