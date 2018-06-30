/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands

import jetbrains.buildServer.dotnet.DotnetCommandType

/**
 * Provides parameters for dotnet test command.
 */
class TestCommandType : DotnetType() {
    override val name: String = DotnetCommandType.Test.id

    override val editPage: String = "editTestParameters.jsp"

    override val viewPage: String = "viewTestParameters.jsp"
}
