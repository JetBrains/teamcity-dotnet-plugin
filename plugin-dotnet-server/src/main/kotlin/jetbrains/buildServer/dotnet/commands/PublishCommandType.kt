/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType

/**
 * Provides parameters for dotnet publish command.
 */
class PublishCommandType : DotnetType() {
    override val name: String = DotnetCommandType.Publish.id

    override val editPage: String = "editPublishParameters.jsp"

    override val viewPage: String = "viewPublishParameters.jsp"
}
