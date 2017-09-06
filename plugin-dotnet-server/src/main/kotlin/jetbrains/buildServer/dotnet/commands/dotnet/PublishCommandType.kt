/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands.dotnet

import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.commands.CommandType

/**
 * Provides parameters for dotnet publish id.
 */
class PublishCommandType : CommandType() {
    override val name: String
        get() = DotnetCommandType.Publish.id

    override val editPage: String
        get() = "editPublishParameters.jsp"

    override val viewPage: String
        get() = "viewPublishParameters.jsp"
}
