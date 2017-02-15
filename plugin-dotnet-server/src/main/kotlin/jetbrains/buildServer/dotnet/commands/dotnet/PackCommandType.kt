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
 * Provides parameters for dotnet pack command.
 */
class PackCommandType : CommandType {
    override val name: String
        get() = DotnetConstants.COMMAND_PACK

    override val editPage: String
        get() = "editPackParameters.jsp"

    override val viewPage: String
        get() = "viewPackParameters.jsp"
}
