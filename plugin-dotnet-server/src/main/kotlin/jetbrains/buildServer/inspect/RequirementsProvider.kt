package jetbrains.buildServer.inspect

import jetbrains.buildServer.requirements.Requirement

interface RequirementsProvider {
    fun getRequirements(parameters: Map<String, String>): Collection<Requirement>
}