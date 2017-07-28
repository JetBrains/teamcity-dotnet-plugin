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
 * Provides arguments to dotnet build command.
 */
class BuildArgumentsProvider(
        _parametersService: ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    override fun getArgumentStrings(): Sequence<String> = buildSequence {
        yield(DotnetConstants.COMMAND_BUILD)

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            yieldAll(_argumentsService.parseToStrings(it))
        }

        parameters(DotnetConstants.PARAM_BUILD_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--framework")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_BUILD_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--configuration")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_BUILD_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--runtime")
                yield(it)
            }
        }

        if (parameters(DotnetConstants.PARAM_BUILD_NON_INCREMENTAL, "").trim().toBoolean()) {
            yield("--no-incremental")
        }

        if (parameters(DotnetConstants.PARAM_BUILD_NO_DEPENDENCIES, "").trim().toBoolean()) {
            yield("--no-dependencies")
        }

        parameters(DotnetConstants.PARAM_BUILD_OUTPUT)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--output")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_BUILD_VERSION_SUFFIX)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--version-suffix")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_VERBOSITY)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--verbosity")
                yield(it)
            }
        }
    }
}