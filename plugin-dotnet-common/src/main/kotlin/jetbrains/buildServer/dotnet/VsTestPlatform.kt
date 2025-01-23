package jetbrains.buildServer.dotnet

enum class VsTestPlatform(val id: String, val description: String) {
    Default("auto", "Auto"),
    x86("x86", "x86"),
    x64("x64", "x64"),
    ARM("ARM", "ARM");

    companion object {
        fun tryParse(id: String): VsTestPlatform? {
            return VsTestPlatform.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}