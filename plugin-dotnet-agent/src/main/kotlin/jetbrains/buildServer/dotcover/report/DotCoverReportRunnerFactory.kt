package jetbrains.buildServer.dotcover.report

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.impl.OSTypeDetector
import jetbrains.buildServer.dotcover.DotCoverWorkflowComposer
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageReportGeneratorRunner
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.toolResolvers.DotnetToolResolver
import jetbrains.buildServer.util.OSType
import java.io.File

class DotCoverReportRunnerFactory(
    private val _factory: DotCoverParametersFactory,
    private val _osTypeDetector: OSTypeDetector,
    private val _dotnetToolResolver: DotnetToolResolver,
) {

    fun getDotCoverReporter(parameters: DotnetCoverageParameters): DotnetCoverageReportGeneratorRunner? {
        val dcp = _factory.createDotCoverParameters(parameters)
        val path: String? = dcp.dotCoverHomePath
        LOG.debug("Path to dotCover home is: $path")

        if (path == null) {
            val msg = "Failed to find path to dotCover"
            LOG.info(msg)
            parameters.getBuildLogger().warning(msg)
            return null
        }

        parameters.resolvePathToTool(path, CoverageConstants.DOTCOVER_WINDOWS_EXECUTABLE)


        val entryPointFileExe = parameters.resolvePathToTool(path, CoverageConstants.DOTCOVER_WINDOWS_EXECUTABLE)
        val entryPointFileSh = parameters.resolvePathToTool(path, CoverageConstants.DOTCOVER_EXECUTABLE)
        val entryPointFileDll = parameters.resolvePathToTool(path, CoverageConstants.DOTCOVER_DLL)

        val dotCoverExecutable = when {
            _osTypeDetector.detect() == OSType.WINDOWS && (entryPointFileExe?.isFile ?: false) -> entryPointFileExe
            entryPointFileSh?.isFile ?: false -> entryPointFileSh
            else -> entryPointFileDll
        }

        if (dotCoverExecutable == null || !dotCoverExecutable.isFile) {
            val msg = "Failed to find dotCover under: $path"
            LOG.warn(msg)
            parameters.getBuildLogger().warning(msg)
            return null
        }

        return DotnetCoverageReportGeneratorRunner(parameters, CoverageConstants.DOTCOVER_TOOL_NAME, dotCoverExecutable!!)
    }

    companion object {
        private val LOG = Logger.getInstance(DotCoverReportRunnerFactory::class.java.name)
    }
}
