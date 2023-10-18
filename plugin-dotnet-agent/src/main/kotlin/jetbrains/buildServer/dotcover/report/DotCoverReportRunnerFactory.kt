package jetbrains.buildServer.dotcover.report

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.ToolCannotBeFoundException
import jetbrains.buildServer.dotcover.DotCoverEntryPointSelector
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageReportGeneratorRunner
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import java.io.File

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

    private fun getEntryPoint() : File {
        try {
            // if report generation has started, then dotCover tool has already been executed during the build
            // and validation of the tool requirements to the agent has already been performed - we can skip it here
            return _entryPointSelector.select(skipRequirementsValidation = true).getOrThrow()
        } catch (e: ToolCannotBeFoundException) {
            val exception = RunBuildException("dotCover report generation failed: " + e.message)
            LOG.error(exception)
            exception.isLogStacktrace = false
            throw exception
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotCoverReportRunnerFactory::class.java.name)
        private const val FILE_EXTENSION_DLL = "dll"
    }
}
