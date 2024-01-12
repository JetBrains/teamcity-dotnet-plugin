

package jetbrains.buildServer.dotnet

enum class ToolPlatform(val description: String) {
    CrossPlatform("Cross-platform"),
    Mono("Mono"),
    Windows("Windows"),
}