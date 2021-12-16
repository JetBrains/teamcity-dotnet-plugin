package jetbrains.buildServer.agent.runner

interface OutputReplacer {
    fun replace(text: String): Sequence<String>
}