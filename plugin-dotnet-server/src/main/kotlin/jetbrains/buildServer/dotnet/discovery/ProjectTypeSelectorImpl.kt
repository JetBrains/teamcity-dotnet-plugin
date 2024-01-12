

package jetbrains.buildServer.dotnet.discovery

import java.util.regex.Pattern

class ProjectTypeSelectorImpl : ProjectTypeSelector {
    override fun select(project: Project): Set<ProjectType> {
        val projectTypes = mutableSetOf<ProjectType>()
        if (isPublishProject(project)) {
            projectTypes.add(ProjectType.Publish)
        }

        if (isTestProject(project)) {
            projectTypes.add(ProjectType.Test)
        }

        if (isVSTestProject(project)) {
            projectTypes.add(ProjectType.Test)
        }

        if (projectTypes.size == 0) {
            projectTypes.add(ProjectType.Unknown)
        }

        return projectTypes
    }

    private fun isTestProject(project: Project): Boolean =
            project.references.filter { TestReferencePattern.matcher(it.id).find() }.any() || isVSTestProject(project)

    private fun isVSTestProject(project: Project): Boolean =
            project.properties.any { "TestProjectType".equals(it.name, true) && "UnitTest".equals(it.value, true) } && project.properties.any { "OutputType".equals(it.name, true) && "Library".equals(it.value, true)}

    private fun isPublishProject(project: Project): Boolean =
            project.generatePackageOnBuild || project.references.filter { PublishReferencePattern.matcher(it.id).find() }.any()

    private companion object {
        private val PublishReferencePattern: Pattern = Pattern.compile("^Microsoft\\.aspnet.+$", Pattern.CASE_INSENSITIVE)
        private val TestReferencePattern: Pattern = Pattern.compile("^Microsoft\\.NET\\.Test\\.Sdk$", Pattern.CASE_INSENSITIVE)
    }
}