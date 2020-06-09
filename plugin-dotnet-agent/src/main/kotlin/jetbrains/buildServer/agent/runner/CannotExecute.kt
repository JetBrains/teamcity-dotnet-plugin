package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.Path

interface CannotExecute {
    fun writeBuildProblemFor(executablePath: Path)
}