/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands.dotnet

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.commands.CommandType

/**
 * Provides parameters for dotnet restore command.
 */
class RestoreCommandType : CommandType {
    override val name: String
        get() = DotnetConstants.COMMAND_RESTORE

    override val editPage: String
        get() = "editRestoreParameters.jsp"

    override val viewPage: String
        get() = "viewRestoreParameters.jsp"
}
