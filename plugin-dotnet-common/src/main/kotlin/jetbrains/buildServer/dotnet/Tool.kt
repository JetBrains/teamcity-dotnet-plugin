package jetbrains.buildServer.dotnet

enum class Tool(val version: Int, val type: ToolType, val platform: ToolPlatform, val bitness: ToolBitness, val description: String) {
    // MSBuild
    MSBuild15CrossPlatform(15, ToolType.MSBuild, ToolPlatform.Any, ToolBitness.Any, "Cross-platform MSBuild"),
    MSBuild15Windows(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "MSBuild v15"),
    MSBuild15WindowsX64(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x64, "MSBuild v15 64-bit"),
    MSBuild15WindowsX86(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x86, "MSBuild v15 32-bit"),
    MSBuild14Windows(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "MSBuild v14"),
    MSBuild14WindowsX64(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x64, "MSBuild v14 64-bit"),
    MSBuild14WindowsX86(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x86, "MSBuild v14 32-bit"),
    MSBuild12Windows(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "MSBuild v12"),
    MSBuild12WindowsX64(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x64, "MSBuild v12 64-bit"),
    MSBuild12WindowsX86(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x86, "MSBuild v12 32-bit"),

    // VSTest
    VSTest15CrossPlatform(15, ToolType.VSTest, ToolPlatform.Any, ToolBitness.Any, "Cross-platform VSTest"),
    VSTest15Windows(15, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "VSTest v15"),
    VSTest14Windows(14, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "VSTest v14"),
    VSTest12Windows(12, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "VSTest v12");

    public val id: String get() = "${type}_${version}_${platform}_${bitness}"

    companion object {
        public fun tryParse(id: String): Tool? {
            return Tool.values().filter { it.id.equals(id, true) }.singleOrNull()
        }
    }
}