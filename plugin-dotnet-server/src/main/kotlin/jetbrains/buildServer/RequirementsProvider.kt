package jetbrains.buildServer

import jetbrains.buildServer.requirements.Requirement

interface RequirementsProvider {
    fun getRequirements(parameters: Map<String, String>): Sequence<Requirement>
}