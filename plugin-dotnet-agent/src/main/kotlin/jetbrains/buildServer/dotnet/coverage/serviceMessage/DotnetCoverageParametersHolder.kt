package jetbrains.buildServer.dotnet.coverage.serviceMessage

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface DotnetCoverageParametersHolder {

    fun getCoverageParameters(): DotnetCoverageParameters

    companion object {
        const val AGENT_LISTENER_ID = "dotNetCoverageDotnetRunner.ParametersHolder"
    }
}
