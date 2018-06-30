@file:Suppress("EXPERIMENTAL_FEATURE_WARNING")

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

class MSBuildVSTestLoggerParametersProvider(
        private val _pathsService: PathsService,
        private val _loggerResolver: LoggerResolver,
        private val _testReportingParameters: TestReportingParameters,
        private val _loggerParameters: LoggerParameters)
    : MSBuildParametersProvider {

    override val parameters: Sequence<MSBuildParameter>
        get() = buildSequence {
            val testReportingMode = _testReportingParameters.mode
            if (testReportingMode.contains(TestReportingMode.Off)) {
                return@buildSequence
            }

            _loggerResolver.resolve(ToolType.VSTest).parentFile?.let {
                yield(MSBuildParameter("VSTestLogger", "logger://teamcity"))
                if (testReportingMode.contains(TestReportingMode.MultiAdapterPath)) {
                    yield(MSBuildParameter("VSTestTestAdapterPath", "${it.absolutePath};."))
                } else {
                    yield(MSBuildParameter("VSTestTestAdapterPath", _pathsService.getPath(PathType.Checkout).absolutePath))
                }
            }

            yield(MSBuildParameter("VSTestVerbosity", _loggerParameters.vsTestVerbosity.id.toLowerCase()))
        }
}