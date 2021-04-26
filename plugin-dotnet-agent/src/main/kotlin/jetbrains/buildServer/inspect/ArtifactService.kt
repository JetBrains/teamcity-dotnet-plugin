package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.Path
import java.io.File

interface ArtifactService {
    fun publish(tool: InspectionTool, artifactSource: File, artifactDestination: Path? = null): Boolean
}