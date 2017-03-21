/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.CommandType

/**
 * Provides parameters for dotnet test command.
 */
class TestCommandType : CommandType() {
    override val name: String
        get() = DotnetConstants.COMMAND_TEST

    override val editPage: String
        get() = "editTestParameters.jsp"

    override val viewPage: String
        get() = "viewTestParameters.jsp"
}
