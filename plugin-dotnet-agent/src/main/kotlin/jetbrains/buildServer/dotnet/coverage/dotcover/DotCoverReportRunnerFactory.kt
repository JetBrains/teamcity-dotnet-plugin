package jetbrains.buildServer.dotnet.coverage.dotcover

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.dotcover.DotCoverEntryPointSelector
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageReportGeneratorRunner
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotCoverReportRunnerFactory(
    private val _dotnetToolResolver: DotnetToolResolver,
    private val _entryPointSelector: DotCoverEntryPointSelector
) {
    fun getDotCoverReporter(parameters: DotnetCoverageParameters): DotnetCoverageReportGeneratorRunner? {
        val entryPointFile = getEntryPoint()

        val profilerHost = when {
            entryPointFile.extension == FILE_EXTENSION_DLL -> _dotnetToolResolver.executable.virtualPath
            else -> null
        }

        return DotnetCoverageReportGeneratorRunner(parameters, CoverageConstants.DOTCOVER_TOOL_NAME, entryPointFile, profilerHost)
    }

    private fun getEntryPoint()=
        _entryPointSelector.select()
            .fold(
                onSuccess = { it },
                onFailure = { when {
                    it is ToolCannotBeFoundException -> {
                        throw RunBuildException("dotCover report generation failed: " + it.message)
                            .let { e -> e.isLogStacktrace = false; e }
                    }
                    else -> throw it
                }}
            )

    companion object {
        private val LOG = Logger.getInstance(DotCoverReportRunnerFactory::class.java.name)
        private const val FILE_EXTENSION_DLL = "dll"
    }
}
