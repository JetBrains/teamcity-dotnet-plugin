package jetbrains.buildServer.runners

import jetbrains.buildServer.agent.BuildRunnerContext

interface BuildStepContext {
    val runnerContext: BuildRunnerContext
}