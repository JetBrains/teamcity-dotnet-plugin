

package jetbrains.buildServer.agent

import jetbrains.buildServer.dotnet.Platform

enum class WindowsRegistryBitness(val id: String) {
    Bitness32("32"),
    Bitness64("64");

    fun getPlatform(isArm: Boolean): Platform = when (this) {
        Bitness32 -> if (isArm) Platform.ARM else Platform.x86
        Bitness64 -> if (isArm) Platform.ARM64 else Platform.x64
    }
}