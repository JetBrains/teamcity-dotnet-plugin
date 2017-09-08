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
 * Provides parameters for dotnet build command.
 */
class BuildCommandType : DotnetType() {
    override val name: String
        get() = DotnetCommandType.Build.id

    override val editPage: String
        get() = "editBuildParameters.jsp"

    override val viewPage: String
        get() = "viewBuildParameters.jsp"
}
