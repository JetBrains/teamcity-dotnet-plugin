package jetbrains.buildServer.dotnet.discovery

data class Solution(val projects: List<Project>, val solution: String = "") {
    val isSimple: Boolean = solution.isBlank()
}