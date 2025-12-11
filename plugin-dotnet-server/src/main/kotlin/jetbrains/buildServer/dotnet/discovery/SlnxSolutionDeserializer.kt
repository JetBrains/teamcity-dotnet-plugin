package jetbrains.buildServer.dotnet.discovery

import jetbrains.buildServer.XmlDocumentService
import jetbrains.buildServer.find
import org.w3c.dom.Element
import java.util.regex.Pattern

class SlnxSolutionDeserializer(
    private val _xmlService: XmlDocumentService,
    private val _projectDeserializer: MSBuildProjectDeserializer,
) : SolutionDeserializer {

    override fun isAccepted(path: String): Boolean = PathPattern.matcher(path).find()

    override fun deserialize(path: String, streamFactory: StreamFactory): Solution =
        streamFactory.tryCreate(path)?.let { input ->
            input.use { stream ->
                val doc = _xmlService.deserialize(stream)

                val projects = doc.find<Element>("//*[local-name()='Project'][@Path]")
                    .map { node -> node.getAttribute("Path") }
                    .filter { projectPath -> projectPath.isNotBlank() }
                    .map { projectPath -> MSBuildSolutionDeserializer.normalizePath(path, projectPath) }
                    .distinct()
                    .flatMap { projectPath ->
                        if (_projectDeserializer.isAccepted(projectPath)) {
                            _projectDeserializer.deserialize(projectPath, streamFactory).projects.asSequence()
                        } else emptySequence()
                    }
                    .distinctBy { project -> project.project }
                    .toList()

                Solution(projects, path)
            }
        } ?: Solution(emptyList())

    private companion object {
        private val PathPattern: Pattern = Pattern.compile("^.+\\.slnx$", Pattern.CASE_INSENSITIVE)
    }
}