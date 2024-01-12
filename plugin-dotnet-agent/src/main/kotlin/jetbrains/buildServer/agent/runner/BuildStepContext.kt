

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.BuildRunnerContext

interface BuildStepContext {
    val isAvailable: Boolean;

    val runnerContext: BuildRunnerContext
}