package jetbrains.buildServer.dotnet.coverage.serviceMessage

interface DotnetCoverageParametersHolder {

    fun getCoverageParameters(): DotnetCoverageParameters

    companion object {
        const val AGENT_LISTENER_ID = "dotNetCoverageDotnetRunner.ParametersHolder"
    }
}
