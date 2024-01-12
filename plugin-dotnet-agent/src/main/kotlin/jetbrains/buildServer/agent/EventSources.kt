

package jetbrains.buildServer.agent

import jetbrains.buildServer.rx.Observable

interface EventSources {
    val buildStartedSource: Observable<Event>
    val buildFinishedSource: Observable<BuildFinished>
    val stepStartedSource: Observable<Event>
    val beforeAgentConfigurationLoadedSource: Observable<EventSources.BeforeAgentConfigurationLoaded>

    class Event {
        companion object {
            public val Shared = Event()
        }
    }

    data class BuildFinished(val buildStatus: BuildFinishedStatus)
    data class BeforeAgentConfigurationLoaded(val agent: BuildAgent)
}