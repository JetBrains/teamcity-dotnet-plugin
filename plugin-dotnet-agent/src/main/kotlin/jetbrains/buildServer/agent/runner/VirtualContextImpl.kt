package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.agent.VirtualContext

class VirtualContextImpl(base: VirtualContext): VirtualContext by base {
    constructor(buildStepContext: BuildStepContext): this(buildStepContext.runnerContext.virtualContext) { }
}