

package jetbrains.buildServer.dotnet

enum class ToolType(val description: String) {
    VisualStudio("Visual Studio"),
    MSBuild("MSBuild"),
    VSTest("VSTest")
}