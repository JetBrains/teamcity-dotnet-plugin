/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.agent

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.runner.BuildStepContext
import jetbrains.buildServer.rx.Observable
import jetbrains.buildServer.rx.Subject
import jetbrains.buildServer.rx.subjectOf
import jetbrains.buildServer.util.EventDispatcher

class AgentLifeCycleEventSourcesImpl(
        events: EventDispatcher<AgentLifeCycleListener>)
    : BuildStepContext, AgentLifeCycleEventSources, AgentLifeCycleAdapter() {

    init {
        events.addListener(this)
    }

    private var _runnerContext: BuildRunnerContext? = null

    override val isAvailable: Boolean
        get() = _runnerContext != null

    override val runnerContext: BuildRunnerContext
        get() = _runnerContext ?: throw RunBuildException("Runner session was not started")

    override val beforeAgentConfigurationLoadedSource = subjectOf<AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent>()

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        beforeAgentConfigurationLoadedSource.onNext(AgentLifeCycleEventSources.BeforeAgentConfigurationLoadedEvent(agent))
        super.beforeAgentConfigurationLoaded(agent)
    }

    override val buildStartedSource: Subject<AgentLifeCycleEventSources.BuildStartedEvent> = subjectOf<AgentLifeCycleEventSources.BuildStartedEvent>()

    override fun buildStarted(build: AgentRunningBuild) {
        _runnerContext = (build as AgentRunningBuildEx).currentRunnerContext
        buildStartedSource.onNext(AgentLifeCycleEventSources.BuildStartedEvent(build))
        super.buildStarted(build)
    }

    override val buildFinishedSource = subjectOf<AgentLifeCycleEventSources.BuildFinishedEvent>()

    override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        try {
            buildFinishedSource.onNext(AgentLifeCycleEventSources.BuildFinishedEvent(build, buildStatus))
            super.beforeBuildFinish(build, buildStatus)
        }
        finally {
            _runnerContext = null
        }
    }
}