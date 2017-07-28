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
 * Provides arguments to dotnet restore command.
 */
class RestoreArgumentsProvider(
        _parametersService: jetbrains.buildServer.runners.ParametersService,
        private val _argumentsService: ArgumentsService)
    : ArgumentsProviderBase(_parametersService, _argumentsService) {

    protected override fun getArgumentStrings(): Sequence<String> = buildSequence {
        yield(DotnetConstants.COMMAND_RESTORE)

        parameters(DotnetConstants.PARAM_PATHS)?.trim()?.let {
            yieldAll(_argumentsService.parseToStrings(it))
        }

        parameters(DotnetConstants.PARAM_RESTORE_PACKAGES)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--packages")
                yield(it)
            }
        }

        parameters(DotnetConstants.PARAM_RESTORE_SOURCE)?.let {
            _argumentsService.parseToStrings(it).forEach {
                yield("--source")
                yield(it)
            }
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_PARALLEL, "").trim().toBoolean()) {
            yield("--disable-parallel")
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_NO_CACHE, "").trim().toBoolean()) {
            yield("--no-cache")
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_IGNORE_FAILED, "").trim().toBoolean()) {
            yield("--ignore-failed-sources")
        }

        if (parameters(DotnetConstants.PARAM_RESTORE_ROOT_PROJECT, "").trim().toBoolean()) {
            yield("--no-dependencies")
        }

        parameters(DotnetConstants.PARAM_RESTORE_CONFIG)?.trim()?.let {
            if (it.isNotBlank()) {
                yield("--configfile")
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