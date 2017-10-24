/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet run command.
 */
class RunCommandType : DotnetType() {
    override val name: String
        get() = DotnetCommandType.Run.id

    override val editPage: String
        get() = "editRunParameters.jsp"

    override val viewPage: String
        get() = "viewRunParameters.jsp"

    override fun getRequirements(parameters: Map<String, String>) = buildSequence {
        yieldAll(super.getRequirements(parameters))
        if (!parameters[DotnetConstants.PARAM_RUN_RUNTIME].isNullOrBlank()) {
            yield(Requirement(DotnetConstants.CONFIG_NAME, "2.0.0", RequirementType.VER_NO_LESS_THAN))
        }
    }
}
