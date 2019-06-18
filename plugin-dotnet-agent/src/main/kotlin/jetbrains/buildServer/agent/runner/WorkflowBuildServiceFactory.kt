/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.agent.runner

import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import org.springframework.beans.factory.BeanFactory

class WorkflowBuildServiceFactory(
        private val runnerType: String,
        private val beanFactory: BeanFactory)
    : MultiCommandBuildSessionFactory, BuildStepContext {
    private var _runnerContext: BuildRunnerContext? = null

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession {
        _runnerContext = runnerContext
        return beanFactory.getBean(WorkflowSessionImpl::class.java)
    }

    override val isAvailable: Boolean
        get() = _runnerContext != null

    override val runnerContext: BuildRunnerContext
        get() = _runnerContext ?: throw RunBuildException("Runner session was not started")

    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo = object : AgentBuildRunnerInfo {
        override fun getType(): String = runnerType

        override fun canRun(config: BuildAgentConfiguration): Boolean = true
    }
}
