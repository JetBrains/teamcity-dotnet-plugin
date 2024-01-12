

package jetbrains.buildServer.inspect

enum class InspectionToolPlatform(val id: String, val displayName: String) {
    WindowsX64("x64", "Windows (x64)"),
    WindowsX86("x86", "Windows (x86)"),
    CrossPlatform("Cross-platform", "Cross-platform");

    companion object {
        fun tryParse(id: String): InspectionToolPlatform? {
            return InspectionToolPlatform.values().singleOrNull() { it.id.equals(id, true) }
        }
    }
}