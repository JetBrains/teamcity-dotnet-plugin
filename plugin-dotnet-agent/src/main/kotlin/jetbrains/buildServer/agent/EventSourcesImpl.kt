

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
        buildFinishedSource.onNext(EventSources.BuildFinished(buildStatus))
        super.beforeBuildFinish(build, buildStatus)
    }

    override fun buildFinished(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
        try {
            super.buildFinished(build, buildStatus)
        } finally {
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