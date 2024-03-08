package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.InvalidProperty

object TestRetryCountValidator {
    fun validate(properties: Map<String, String>) = sequence {
        val key = DotnetConstants.PARAM_TEST_RETRY_MAX_RETRIES
        val propertyValue = properties[key]
        if (propertyValue.isNullOrBlank() || ReferencesResolverUtil.mayContainReference(propertyValue))
            return@sequence

        propertyValue.toIntOrNull().let {
            if (it == null || it < 0) {
                yield(InvalidProperty(key, DotnetConstants.VALIDATION_INVALID_TEST_RETRY))
            }
        }
    }
}