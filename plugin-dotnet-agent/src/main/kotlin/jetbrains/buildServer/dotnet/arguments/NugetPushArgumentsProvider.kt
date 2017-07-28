/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService
import jetbrains.buildServer.runners.ParametersService
import kotlin.coroutines.experimental.buildSequence

/**
 * Provides arguments to dotnet nuget push command.
 */
class NugetPushArgumentsProvider(
        _parametersService: ParametersService,
        _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    protected override fun getArgumentStrings(): Sequence<String> = buildSequence {
        yieldAll(jetbrains.buildServer.util.StringUtil.split(DotnetConstants.COMMAND_NUGET_PUSH))

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_PUSH_API_KEY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--api-key")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_PUSH_SOURCE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--source")
                yield(it)
            }
        }

        if (parameters(DotnetConstants.PARAM_NUGET_PUSH_NO_SYMBOLS, "").trim().toBoolean()) {
            yield("--no-symbols")
            yield("true")
        }

        if (parameters(DotnetConstants.PARAM_NUGET_PUSH_NO_BUFFER, "").trim().toBoolean()) {
            yield("--disable-buffering")
            yield("true")
        }
    }
}
