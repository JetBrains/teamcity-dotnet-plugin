package jetbrains.buildServer.dotnet.discovery

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.DotnetConstants
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

        if (projectTypes.size == 0) {
            projectTypes.add(ProjectType.Unknown)
        }

        return projectTypes
    }

    private fun isTestProject(project: Project): Boolean =
            project.references.filter { TestReferencePattern.matcher(it.id).find() }.any()

    private fun isPublishProject(project: Project): Boolean =
            project.generatePackageOnBuild || project.references.filter { PublishReferencePattern.matcher(it.id).find() }.any()

    private companion object {
        private val PublishReferencePattern: Pattern = Pattern.compile("^Microsoft\\.aspnet.+$", Pattern.CASE_INSENSITIVE)
        private val TestReferencePattern: Pattern = Pattern.compile("^Microsoft\\.NET\\.Test\\.Sdk$", Pattern.CASE_INSENSITIVE)
    }
}