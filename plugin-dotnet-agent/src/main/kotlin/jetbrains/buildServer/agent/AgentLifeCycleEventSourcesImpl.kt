package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.subjectOf
import jetbrains.buildServer.util.EventDispatcher

class AgentLifeCycleEventSourcesImpl(
        events: EventDispatcher<AgentLifeCycleListener>)
    : AgentLifeCycleEventSources, AgentLifeCycleAdapter() {

    init {
        events.addListener(this)
    }

    override val beforeAgentConfigurationLoadedSource = subjectOf<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>()

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        beforeAgentConfigurationLoadedSource.onNext(AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent(agent))
        super.beforeAgentConfigurationLoaded(agent)
    }

    override val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()

    override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(build, buildStatus))
        super.beforeBuildFinish(build, buildStatus)
    }
}