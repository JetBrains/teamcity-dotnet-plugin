package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.util.EventDispatcher

class DotnetCoverageProcessorEventsSubscription(
    events: EventDispatcher<AgentLifeCycleListener?>,
    processor: DotnetCoverageProcessor) {

    init {
        events.addListener(object : AgentLifeCycleAdapter() {
            override fun buildStarted(runningBuild: AgentRunningBuild) {
                processor.cleanupState()
            }

            override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
                processor.processCoverageOnBuildFinish()
                processor.cleanupState()
            }
        })
    }
}
