package jetbrains.buildServer.coverage.agent.serviceMessage

interface CoverageServiceMessageSetup {
    fun addPropertyMapping(serviceMessageKey: String, runnerParameter: String)
}