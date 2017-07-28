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
 * Provides arguments to dotnet test command.
 */
class TestArgumentsProvider(
        _parametersService: jetbrains.buildServer.runners.ParametersService,
        _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    protected override fun getArgumentStrings(): Sequence<String> = buildSequence {
        yield(DotnetConstants.COMMAND_TEST)

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            if (it.isNotBlank()) {
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_TEST_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--framework")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_TEST_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--configuration")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_TEST_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--runtime")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_TEST_OUTPUT)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--output")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_TEST_TEMP)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--build-base-path")
                yield(it)
            }
        }

        if (parameters(DotnetConstants.PARAM_TEST_NO_BUILD, "").trim().toBoolean()) {
            yield("--no-build")
        }
    }
}