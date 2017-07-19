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
 * Provides arguments to dotnet publish command.
 */
class PublishArgumentsProvider(
        _parametersService: jetbrains.buildServer.runners.ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    protected override fun getArgumentStrings(): Sequence<String> = buildSequence {
        yield(DotnetConstants.COMMAND_PUBLISH)

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            yieldAll(_argumentsService.parseToStrings(it))
        }

        parameters(DotnetConstants.PARAM_PUBLISH_FRAMEWORK)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--framework")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--configuration")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_RUNTIME)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--runtime")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_OUTPUT)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--output")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_PUBLISH_VERSION_SUFFIX)?.trim()?.let {
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
