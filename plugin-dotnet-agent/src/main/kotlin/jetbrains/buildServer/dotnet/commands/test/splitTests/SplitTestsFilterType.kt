package jetbrains.buildServer.dotnet.commands.test.splitTests

enum class SplitTestsFilterType(val id: String) {
    Includes("includes"),
    Excludes("excludes"),
}