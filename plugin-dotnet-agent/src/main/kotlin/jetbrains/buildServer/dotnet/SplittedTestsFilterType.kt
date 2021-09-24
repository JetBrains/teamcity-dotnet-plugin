package jetbrains.buildServer.dotnet

enum class SplittedTestsFilterType(val id: String) {
    IncludeAll("INCLUDE ALL"),
    ExcludeAll("EXCLUDE ALL"),
    Include("INCLUDE"),
    Exclude("EXCLUDE");

    companion object {
        fun tryParse(id: String): SplittedTestsFilterType? {
            return SplittedTestsFilterType.values().singleOrNull { it.id.equals(id, true) }
        }
    }
}