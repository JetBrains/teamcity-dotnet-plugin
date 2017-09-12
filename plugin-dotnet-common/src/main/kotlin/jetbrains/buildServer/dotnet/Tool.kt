package jetbrains.buildServer.dotnet

enum class Tool(val version: Int, val type: ToolType, val platform: ToolPlatform, val bitness: ToolBitness, val description: String) {
    // MSBuild
    MSBuild15DotnetCore(15, ToolType.MSBuild, ToolPlatform.DotnetCore, ToolBitness.Any, "for .NET Core"),
    MSBuild15Mono(15, ToolType.MSBuild, ToolPlatform.Mono, ToolBitness.Any, "for Mono"),
    MSBuild15Windows(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "15"),
    MSBuild15WindowsX64(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x64, "15 64-bit"),
    MSBuild15WindowsX86(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x86, "15 32-bit"),
    MSBuild14Windows(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "14"),
    MSBuild14WindowsX64(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x64, "14 64-bit"),
    MSBuild14WindowsX86(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x86, "14 32-bit"),
    MSBuild12Windows(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "12"),
    MSBuild12WindowsX64(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x64, "12 64-bit"),
    MSBuild12WindowsX86(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.x86, "12 32-bit"),

    // VSTest
    VSTest15CrossPlatform(15, ToolType.VSTest, ToolPlatform.DotnetCore, ToolBitness.Any, "Cross-platform"),
    VSTest15Windows(15, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "15"),
    VSTest14Windows(14, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "14"),
    VSTest12Windows(12, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "12");

    public val id: String get() = "${type}_${version}_${platform}_${bitness}"

    companion object {
        public fun tryParse(id: String): Tool? {
            return Tool.values().filter { it.id.equals(id, true) }.singleOrNull()
        }
    }
}