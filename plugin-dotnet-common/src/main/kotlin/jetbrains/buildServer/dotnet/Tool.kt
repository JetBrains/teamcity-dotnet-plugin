package jetbrains.buildServer.dotnet

enum class Tool(val version: Int, val type: ToolType, val platform: ToolPlatform, val bitness: ToolBitness, val description: String) {
    // Visual Studio
    VisualStudio2017(2017, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2017"),
    VisualStudio2015(2015, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2015"),
    VisualStudio2013(2013, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2013"),
    VisualStudio2012(2012, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2012"),
    VisualStudio2010(2010, ToolType.VisualStudio, ToolPlatform.Windows, ToolBitness.Any, "Visual Studio 2010"),

    // MSBuild
    MSBuild15DotnetCore(15, ToolType.MSBuild, ToolPlatform.CrossPlatform, ToolBitness.Any, ".NET Core"),
    MSBuild15Mono(15, ToolType.MSBuild, ToolPlatform.Mono, ToolBitness.Any, "Mono"),
    MSBuild15Windows(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "15.0"),
    MSBuild15WindowsX64(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "15.0 64-bit"),
    MSBuild15WindowsX86(15, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "15.0 32-bit"),
    MSBuild14Windows(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "14.0"),
    MSBuild14WindowsX64(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "14.0 64-bit"),
    MSBuild14WindowsX86(14, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "14.0 32-bit"),
    MSBuild12Windows(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.Any, "12.0"),
    MSBuild12WindowsX64(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X64, "12.0 64-bit"),
    MSBuild12WindowsX86(12, ToolType.MSBuild, ToolPlatform.Windows, ToolBitness.X86, "12.0 32-bit"),

    // VSTest
    VSTest15CrossPlatform(15, ToolType.VSTest, ToolPlatform.CrossPlatform, ToolBitness.Any, "Cross-platform"),
    VSTest15Windows(15, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "15"),
    VSTest14Windows(14, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "14"),
    VSTest12Windows(12, ToolType.VSTest, ToolPlatform.Windows, ToolBitness.Any, "12");

    val id: String get() = "${type}_${version}_${platform}_$bitness"

    companion object {
        fun tryParse(id: String): Tool? {
            return Tool.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}