package jetbrains.buildServer.dotnet

enum class SplittedTestsFilterType(val id: String) {
    Includes("includes"),
    Excludes("excludes"),
}