package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.requirements.SdkBasedRequirementFactory
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides command-specific resources
 */
abstract class CommandType(
    private val _sdkBasedRequirementFactory: SdkBasedRequirementFactory,
) {
    abstract val name: String

    open val description: String get() = name

    abstract val editPage: String

    abstract val viewPage: String

    open fun validateProperties(properties: Map<String, String>): Sequence<InvalidProperty> = sequence {
        properties[DotnetConstants.PARAM_REQUIRED_SDK]?.let { sdk ->
            yieldAll(validateRequiredSDK(sdk))
        }
    }

    private fun validateRequiredSDK(requiredSdkProperty: String): Sequence<InvalidProperty> = sequence {
        val invalidSdks = getSdkBasedRequirements(requiredSdkProperty)
            .filter { it.requirement == null }  // if the requirement cannot be provided - the entered SDK is invalid
            .joinToString(",") { "\"${it.sdkVersion}\"" }
            .ifBlank { return@sequence }

        yield(InvalidProperty(DotnetConstants.PARAM_REQUIRED_SDK, "Invalid SDK versions: $invalidSdks"))
    }

    private fun getSdkBasedRequirements(requiredSdkProperty: String) =
        requiredSdkProperty
            .split(" ", "\n", ";")
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { RequirementResult(it, _sdkBasedRequirementFactory.tryCreate(it)) }

    private data class RequirementResult(val sdkVersion: String, val requirement: Requirement?)
}
