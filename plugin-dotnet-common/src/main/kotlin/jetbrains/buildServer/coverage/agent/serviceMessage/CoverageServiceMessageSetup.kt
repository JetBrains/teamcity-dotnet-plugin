package jetbrains.buildServer.coverage.agent.serviceMessage

@Deprecated("Deprecated after task TW-85039. Needed for backward compatibility")
interface CoverageServiceMessageSetup {
    fun addPropertyMapping(serviceMessageKey: String, runnerParameter: String)
}