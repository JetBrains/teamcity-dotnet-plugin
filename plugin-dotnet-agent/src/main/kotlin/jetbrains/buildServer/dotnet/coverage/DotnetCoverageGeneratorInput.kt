package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

class DotnetCoverageGeneratorInput(
    private val _generator: DotnetCoverageReportGenerator,
    private val _coverageType: String,
    private val _params: List<DotnetCoverageParameters>,
    private val _files: List<File>) {

    private val _parameters: DotnetCoverageParameters = _params.iterator().next()

    fun getLogger(): BuildProgressLogger {
        return getFirstStepParameters().getBuildLogger()
    }

    val generator: DotnetCoverageReportGenerator get() = _generator

    val coverageType: String get() = _coverageType

    fun getFirstStepParameters(): DotnetCoverageParameters {
        return _parameters
    }

    fun getParameters(): List<DotnetCoverageParameters> {
        return _params
    }

    fun getFiles(): List<File> {
        return _files
    }
}

