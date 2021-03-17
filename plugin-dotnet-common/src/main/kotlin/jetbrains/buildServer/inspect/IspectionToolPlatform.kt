package jetbrains.buildServer.inspect

import jetbrains.buildServer.dotnet.Platform

enum class IspectionToolPlatform(val id: String) {
    X86("x86"),
    X64("x64"),
    CrossPlatform("Cross-platform");

    companion object {
        fun tryParse(id: String): IspectionToolPlatform? {
            return IspectionToolPlatform.values().singleOrNull() { it.id.equals(id, true) }
        }
    }
}