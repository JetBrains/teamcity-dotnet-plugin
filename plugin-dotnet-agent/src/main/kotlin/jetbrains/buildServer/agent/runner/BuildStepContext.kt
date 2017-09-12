package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildRunnerContext

interface BuildStepContext {
    val runnerContext: BuildRunnerContext
}