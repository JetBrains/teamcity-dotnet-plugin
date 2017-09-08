/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.CommandType

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
}
