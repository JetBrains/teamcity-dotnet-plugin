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
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.util.EventDispatcher
import org.springframework.beans.factory.BeanFactory

class EventSourcesImpl(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _beanFactory: BeanFactory)
    : BuildStepContext, EventSources, AgentLifeCycleAdapter() {

    private var _subscription: Disposable? = null
    private var _runnerContext: BuildRunnerContext? = null

    init {
        events.addListener(this)
    }

    override val isAvailable: Boolean
        get() = _runnerContext != null

    override val runnerContext: BuildRunnerContext
        get() = _runnerContext ?: throw RunBuildException("Runner session was not started")

    override val beforeAgentConfigurationLoadedSource = subjectOf<EventSources.BeforeAgentConfigurationLoadedEvent>()

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        val observers = (_beanFactory.getBean(EventObservers::class.java) as EventObservers).toList()
        val subscriptions = observers.map { it.subscribe(this) }.toTypedArray()
        _subscription = disposableOf(*subscriptions)

        beforeAgentConfigurationLoadedSource.onNext(EventSources.BeforeAgentConfigurationLoadedEvent(agent))
        super.beforeAgentConfigurationLoaded(agent)
    }

    override fun agentShutdown() {
        _subscription?.dispose()
        super.agentShutdown()
    }

    override val buildStartedSource: Subject<EventSources.BuildStartedEvent> = subjectOf<EventSources.BuildStartedEvent>()

    override fun buildStarted(build: AgentRunningBuild) {
        _runnerContext = (build as AgentRunningBuildEx).currentRunnerContext
        buildStartedSource.onNext(EventSources.BuildStartedEvent(build))
        super.buildStarted(build)
    }

    override val buildFinishedSource = subjectOf<EventSources.BuildFinishedEvent>()

    override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        try {
            buildFinishedSource.onNext(EventSources.BuildFinishedEvent(build, buildStatus))
            super.beforeBuildFinish(build, buildStatus)
        }
        finally {
            _runnerContext = null
        }
    }

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        _runnerContext = runner
        super.beforeRunnerStart(runner)
    }
}