package jetbrains.buildServer.dotnet.coverage.dotcover

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
class DotCoverParametersFactory {

    fun createDotCoverParameters(build: DotnetCoverageParameters): DotCoverParameters {
        return object : DotCoverParameters() {
            override fun getValue(key: String): String? {
                return build.getRunnerParameter(key)
            }
        }
    }
}
