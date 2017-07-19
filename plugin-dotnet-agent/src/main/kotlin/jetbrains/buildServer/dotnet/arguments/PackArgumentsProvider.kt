/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.arguments

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.runners.ArgumentsService

/**
 * Provides arguments to dotnet pack command.
 */
class PackArgumentsProvider(
        _parametersService: jetbrains.buildServer.runners.ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    protected override fun getArgumentStrings(): Sequence<String> {
        return kotlin.coroutines.experimental.buildSequence {
            yield(DotnetConstants.COMMAND_PACK)

            parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
                yieldAll(_argumentsService.parseToStrings(it))
            }

            parameters(DotnetConstants.PARAM_PACK_CONFIG)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield("--configuration")
                    yield(it)
                }
            }

            parameters(DotnetConstants.PARAM_PACK_OUTPUT)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield("--output")
                    yield(it)
                }
            }

            parameters(DotnetConstants.PARAM_PACK_TEMP)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield("--build-base-path")
                    yield(it)
                }
            }

            parameters(DotnetConstants.PARAM_PACK_VERSION_SUFFIX)?.trim()?.let {
                if (it.isNotBlank()) {
                    yield("--version-suffix")
                    yield(it)
                }
            }

            if (parameters(DotnetConstants.PARAM_PACK_NO_BUILD, "").trim().toBoolean()) {
                yield("--no-build")
            }

            if (parameters(DotnetConstants.PARAM_PACK_SERVICEABLE, "").trim().toBoolean()) {
                yield("--serviceable")
            }
        }
    }
}
