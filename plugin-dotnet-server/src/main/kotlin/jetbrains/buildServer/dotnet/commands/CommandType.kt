/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.serverSide.InvalidProperty
import org.springframework.beans.factory.BeanFactory

/**
 * Provides command-specific resources.
 */
abstract class CommandType {
    abstract val name: String

    open val description: String
        get() = name

    abstract val editPage: String

    abstract val viewPage: String

    open fun validateProperties(properties: Map<String, String>): Sequence<InvalidProperty> = emptySequence()

    open fun getRequirements(parameters: Map<String, String>, factory: BeanFactory): Sequence<Requirement> = emptySequence()

    protected fun isDocker(parameters: Map<String, String>) = !parameters[DotnetConstants.PARAM_DOCKER_IMAGE].isNullOrEmpty()
}
