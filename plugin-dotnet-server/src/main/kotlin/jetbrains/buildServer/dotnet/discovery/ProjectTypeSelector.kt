package jetbrains.buildServer.dotnet.discovery

interface ProjectTypeSelector {
    fun select(project: Project): Set<ProjectType>
}