/*
 * Copyright 2000-2022 JetBrains s.r.o.
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
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.rx.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.positioning.PositionAware
import jetbrains.buildServer.util.positioning.PositionConstraint
import org.springframework.beans.factory.BeanFactory

class EventSourcesImpl(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _beanFactory: BeanFactory)
    : BuildStepContext, EventSources, AgentLifeCycleAdapter(), DirectoryCleanersProvider, PositionAware {

    private var _subscription: Disposable? = null
    private var _runnerContext: BuildRunnerContext? = null

    init {
        events.addListener(this)
    }

    override fun getOrderId() = DotnetConstants.RUNNER_TYPE

    override fun getConstraint() = PositionConstraint.first()

    override val isAvailable: Boolean
        get() = _runnerContext != null

    override val runnerContext: BuildRunnerContext
        get() = _runnerContext ?: throw RunBuildException("Runner session was not started")

    override val beforeAgentConfigurationLoadedSource = subjectOf<EventSources.BeforeAgentConfigurationLoaded>()

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        val observers = (_beanFactory.getBean(EventObservers::class.java) as EventObservers).toList()
        val subscriptions = observers.map { it.subscribe(this) }.toTypedArray()
        _subscription = disposableOf(*subscriptions)

        beforeAgentConfigurationLoadedSource.onNext(EventSources.BeforeAgentConfigurationLoaded(agent))
        super.beforeAgentConfigurationLoaded(agent)
    }

    override fun agentShutdown() {
        _subscription?.dispose()
        super.agentShutdown()
    }

    override val buildStartedSource: Subject<EventSources.Event> = subjectOf<EventSources.Event>()

    override fun buildStarted(build: AgentRunningBuild) {
        _runnerContext = (build as AgentRunningBuildEx).currentRunnerContext
        buildStartedSource.onNext(EventSources.Event.Shared)
        super.buildStarted(build)
    }

    override val buildFinishedSource = subjectOf<EventSources.BuildFinished>()

    override fun beforeBuildFinish(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        try {
            buildFinishedSource.onNext(EventSources.BuildFinished(buildStatus))
            super.beforeBuildFinish(build, buildStatus)
        }
        finally {
            _runnerContext = null
        }
    }

    override val stepStartedSource = subjectOf<EventSources.Event>()

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        _runnerContext = runner
        stepStartedSource.onNext(EventSources.Event.Shared)
        super.beforeRunnerStart(runner)
    }

    override fun getCleanerName() = DotnetConstants.CLEANER_NAME + " initializer"

    override fun registerDirectoryCleaners(context: DirectoryCleanersProviderContext, registry: DirectoryCleanersRegistry) {
        _runnerContext = (context.runningBuild as AgentRunningBuildEx).currentRunnerContext
    }
}