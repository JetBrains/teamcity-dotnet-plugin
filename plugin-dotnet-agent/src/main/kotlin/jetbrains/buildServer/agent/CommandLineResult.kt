package jetbrains.buildServer.agent

data class CommandLineResult(
        val exitCode: Int,
        val standardOutput: Collection<String>,
        val errorOutput: Collection<String>) {
}