/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotCoverConstants
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType
import jetbrains.buildServer.serverSide.InvalidProperty
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides parameters for dotnet test command.
 */
class TestCommandType(private val _dotCoverInfoProvider: DotCoverInfoProvider) : DotnetType() {
    override val name: String
        get() = DotnetCommandType.Test.id

    override val editPage: String
        get() = "editTestParameters.jsp"

    override val viewPage: String
        get() = "viewTestParameters.jsp"

    override fun validateProperties(properties: Map<String, String>): Collection<InvalidProperty> {
        val invalidProperties = arrayListOf<InvalidProperty>()
        if (_dotCoverInfoProvider.isCoverageEnabled(properties)) {
            if (properties[DotCoverConstants.PARAM_HOME].isNullOrBlank()) {
                invalidProperties.add(InvalidProperty(DotCoverConstants.PARAM_HOME, DotnetConstants.VALIDATION_EMPTY))
            }
        }

        return invalidProperties
    }

    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    override fun getRequirements(runParameters: Map<String, String>): Sequence<Requirement> = buildSequence {
        if (_dotCoverInfoProvider.isCoverageEnabled(runParameters)) {
            yieldAll(super.getRequirements(runParameters))
            yield(Requirement("teamcity.agent.jvm.os.name", "Windows", RequirementType.STARTS_WITH))
        }
    }
}
