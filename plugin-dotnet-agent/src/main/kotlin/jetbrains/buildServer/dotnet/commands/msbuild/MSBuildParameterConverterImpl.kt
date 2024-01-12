

package jetbrains.buildServer.dotnet.commands.msbuild

import jetbrains.buildServer.agent.Environment
import jetbrains.buildServer.agent.runner.ParameterType
import jetbrains.buildServer.agent.runner.ParametersService
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.util.OSType

class MSBuildParameterConverterImpl(
    private val _parameterService: ParametersService,
    private val _environment: Environment
) : MSBuildParameterConverter {
    private val isParametersEscapingEnabled
        get() =
            _parameterService.tryGetParameter(
                ParameterType.Configuration,
                DotnetConstants.PARAM_MSBUILD_PARAMETERS_ESCAPE
            )
                ?.let { it.trim().equals("true", ignoreCase = true) }
                ?: false
    private val isTrailingBackslashQuotationDisabled
        get() =
            _parameterService.tryGetParameter(
                ParameterType.Configuration,
                DotnetConstants.PARAM_MSBUILD_DISABLE_TRAILING_BACKSLASH_QUOTATION
            )
                ?.let { it.trim().equals("true", ignoreCase = true) }
                ?: false

    override fun convert(parameters: Sequence<MSBuildParameter>): Sequence<String> {
        // copy to not have behaviour changes in the middle of params sequence processing
        val isParametersEscapingEnabled = isParametersEscapingEnabled
        val isTrailingBackslashQuotationDisabled = isTrailingBackslashQuotationDisabled

        return parameters
            .filter { parameter -> parameter.name.isNotBlank() && parameter.value.isNotBlank() }
            .map { parameter ->
                val normalizedName = MSBuildParameterNormalizer.normalizeName(parameter.name)

                val fullEscaping = isParametersEscapingEnabled || parameter.type == MSBuildParameterType.Predefined
                val quoteTrailingBackslash = !isTrailingBackslashQuotationDisabled && _environment.os == OSType.WINDOWS
                val normalizedValue = MSBuildParameterNormalizer.normalizeAndQuoteValue(
                    parameter.value,
                    fullEscaping,
                    quoteTrailingBackslash
                )

                "-p:${normalizedName}=${normalizedValue}"
            }
    }
}