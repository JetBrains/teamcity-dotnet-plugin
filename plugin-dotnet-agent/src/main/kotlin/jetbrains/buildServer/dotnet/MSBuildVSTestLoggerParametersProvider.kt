package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.runner.PathType
import jetbrains.buildServer.agent.runner.PathsService
import kotlin.coroutines.experimental.buildSequence

class MSBuildVSTestLoggerParametersProvider: MSBuildParametersProvider {

    override val parameters: Sequence<MSBuildParameter>
        get() = buildSequence {
            yield(MSBuildParameter("VSTestLogger", "logger://teamcity"))
            yield(MSBuildParameter("VSTestTestAdapterPath", "."))
        }
}