package jetbrains.buildServer.dotcover.report

import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters

class DotCoverParametersFactory {

    fun createDotCoverParameters(build: DotnetCoverageParameters): DotCoverParameters {
        return object : DotCoverParameters() {
            override fun getValue(key: String): String? {
                return build.getRunnerParameter(key)
            }
        }
    }
}
