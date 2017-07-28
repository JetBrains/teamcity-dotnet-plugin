/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet run command.
 */
class RunArgumentsProvider(
        _parametersService: jetbrains.buildServer.runners.ParametersService,
        _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    protected override fun getArgumentStrings(): Sequence<String> = buildSequence {
        yield(DotnetConstants.COMMAND_RUN)

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            yield("--project")
            yield(it)
        }

        parameters(DotnetConstants.PARAM_RUN_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--framework")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_RUN_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--configuration")
                yield(it)
            }
        }
    }
}