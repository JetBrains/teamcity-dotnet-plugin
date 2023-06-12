package jetbrains.buildServer.dotcover.report

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.impl.OSTypeDetector
import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.dotnet.coverage.DotnetCoverageReportGeneratorRunner
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.util.OSType
import java.io.File

class DotCoverReportRunnerFactory(
    private val _factory: DotCoverParametersFactory,
    private val _osTypeDetector: OSTypeDetector
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

        val dotCoverExecutable: String = if (_osTypeDetector.detect() == OSType.WINDOWS) {
            CoverageConstants.DOTCOVER_WINDOWS_EXECUTABLE
        } else {
            CoverageConstants.DOTCOVER_EXECUTABLE
        }

        val exe: File? = parameters.resolvePathToTool(path, dotCoverExecutable)
        if (exe == null || !exe.isFile) {
            val msg = "Failed to find dotCover under: $path"
            LOG.warn(msg)
            parameters.getBuildLogger().warning(msg)
            return null
        }

        return DotnetCoverageReportGeneratorRunner(parameters, CoverageConstants.DOTCOVER_TOOL_NAME, exe)
    }

    companion object {
        private val LOG = Logger.getInstance(DotCoverReportRunnerFactory::class.java.name)
    }
}
