/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.runners.CommandLineArgument

/**
 * Provides arguments to the utility.
 */
interface ArgumentsProvider {
    fun getArguments(): Sequence<CommandLineArgument>
}
