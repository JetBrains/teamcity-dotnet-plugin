package jetbrains.buildServer.dotnet

enum class SplittedTestsFilterType(val id: String) {
    IncludeAll("include all"),
    ExcludeAll("exclude all"),
    Includes("includes"),
    Excludes("excludes");

    companion object {
        fun of(name: String): SplittedTestsFilterType = values().firstOrNull() { it.id == name.trim() } ?: SplittedTestsFilterType.IncludeAll
    }
}