

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.dotnet.DotnetConstants
import org.springframework.beans.factory.BeanFactory

class WorkflowBuildServiceFactory(
        private val _runnerType: String,
        private val _beanFactory: BeanFactory)
    : MultiCommandBuildSessionFactory {

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession {
        return _beanFactory.getBean(WorkflowSessionImpl::class.java)
    }

    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo = object : AgentBuildRunnerInfo {
        override fun getType(): String = _runnerType

        override fun canRun(config: BuildAgentConfiguration): Boolean = true
    }
}