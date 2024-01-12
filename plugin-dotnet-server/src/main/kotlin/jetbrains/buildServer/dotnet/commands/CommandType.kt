

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.RequirementFactory
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import org.springframework.beans.factory.BeanFactory

/**
 * Provides command-specific resources.
 */
abstract class CommandType(
        private val _requirementFactory: RequirementFactory) {
    abstract val name: String

    open val description: String
        get() = name

    abstract val editPage: String

    abstract val viewPage: String

    private fun getRequirements(parameters: Map<String, String>) =
            parameters[DotnetConstants.PARAM_REQUIRED_SDK]?.let {
                it
                        .split(" ", "\n", ";")
                        .asSequence()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { RequirementResult(it, _requirementFactory.tryCreate(it)) }
            } ?: emptySequence()

    open fun validateProperties(properties: Map<String, String>): Sequence<InvalidProperty> =
            getRequirements(properties)
                    .filter { it.requirement == null }
                    .map { it }
                    .joinToString(",") { "\"${it.sdkVersion}\"" }
                    .let {
                        sequence {
                            if (it.isNotBlank()) {
                               yield(InvalidProperty(DotnetConstants.PARAM_REQUIRED_SDK, "Invalid version: $it"))
                            }
                        }
                    }

    open fun getRequirements(parameters: Map<String, String>, factory: BeanFactory) =
        getRequirements(parameters).mapNotNull { it.requirement }

    private data class RequirementResult(val sdkVersion: String, val requirement: Requirement?)
}