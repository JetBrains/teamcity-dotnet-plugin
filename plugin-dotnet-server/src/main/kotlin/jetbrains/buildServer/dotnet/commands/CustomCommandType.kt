/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.serverSide.InvalidProperty

/**
 * Provides parameters for dotnet %custom% command.
 */
class CustomCommandType : DotnetType() {
    override val name: String = DotnetCommandType.Custom.id

    override val description: String = "<custom>"

    override val editPage: String = "editCustomParameters.jsp"

    override val viewPage: String = "viewCustomParameters.jsp"

    override fun validateProperties(properties: Map<String, String>) = sequence {
        yieldAll(super.validateProperties(properties))

        if (properties[DotnetConstants.PARAM_ARGUMENTS].isNullOrBlank()) {
            yield(InvalidProperty(DotnetConstants.PARAM_ARGUMENTS, DotnetConstants.VALIDATION_EMPTY))
        }
    }
}
