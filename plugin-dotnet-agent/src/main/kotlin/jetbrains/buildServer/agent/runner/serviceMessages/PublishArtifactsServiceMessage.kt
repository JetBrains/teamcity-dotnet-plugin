package jetbrains.buildServer.agent.runner.serviceMessages

import jetbrains.buildServer.ArtifactsConstants
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageTypes
import java.io.File

class PublishArtifactsServiceMessage(
    artifactPath: String, destinationArtifactDirectory: String
) : ServiceMessage(
    ServiceMessageTypes.PUBLISH_ARTIFACTS,
    "$artifactPath => ${File(ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR, destinationArtifactDirectory).path}"
)