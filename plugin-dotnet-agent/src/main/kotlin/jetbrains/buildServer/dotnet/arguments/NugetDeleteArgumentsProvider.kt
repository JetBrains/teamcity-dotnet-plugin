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
 * Provides arguments to dotnet nuget delete command.
 */
class NugetDeleteArgumentsProvider(
        _parametersService: jetbrains.buildServer.runners.ParametersService,
        _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    protected override fun getArgumentStrings(): Sequence<String> = buildSequence {
        yieldAll(jetbrains.buildServer.util.StringUtil.split(DotnetConstants.COMMAND_NUGET_DELETE))

        parameters(DotnetConstants.PARAM_NUGET_DELETE_ID)?.trim()?.let {
            if (it.isNotBlank()) {
                yieldAll(jetbrains.buildServer.util.StringUtil.split(it))
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_DELETE_API_KEY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--api-key")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_NUGET_DELETE_SOURCE)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--source")
                yield(it)
            }
        }

        yield("--non-interactive")
    }
}