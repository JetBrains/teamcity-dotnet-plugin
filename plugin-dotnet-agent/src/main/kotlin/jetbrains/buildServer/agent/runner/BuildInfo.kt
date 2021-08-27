package jetbrains.buildServer.agent.runner

interface BuildInfo {
    val id: String

    val name: String

    val runType: String
}