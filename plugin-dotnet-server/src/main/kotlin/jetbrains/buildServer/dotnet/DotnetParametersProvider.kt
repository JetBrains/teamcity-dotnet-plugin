/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.dotnet.commands.CommandType
import jetbrains.buildServer.dotnet.commands.dotnet.*

/**
 * Provides parameters for dotnet runner.
 */
class DotnetParametersProvider {

    val types: List<CommandType>

    init {
        types = listOf(
                BuildCommandType(),
                PackCommandType(),
                PublishCommandType(),
                RestoreCommandType(),
                TestCommandType())
    }

    val commandKey: String
        get() = DotnetConstants.PARAM_COMMAND

    val pathsKey: String
        get() = DotnetConstants.PARAM_PATHS

    val argumentsKey: String
        get() = DotnetConstants.PARAM_ARGUMENTS

    val verbosityKey: String
        get() = DotnetConstants.PARAM_VERBOSITY

    val restorePackagesKey: String
        get() = DotnetConstants.PARAM_RESTORE_PACKAGES

    val restoreParallelKey: String
        get() = DotnetConstants.PARAM_RESTORE_PARALLEL

    val restoreSourceKey: String
        get() = DotnetConstants.PARAM_RESTORE_SOURCE

    val buildFrameworkKey: String
        get() = DotnetConstants.PARAM_BUILD_FRAMEWORK

    val buildArchKey: String
        get() = DotnetConstants.PARAM_BUILD_ARCH

    val buildConfigKey: String
        get() = DotnetConstants.PARAM_BUILD_CONFIG

    val buildRuntimeKey: String
        get() = DotnetConstants.PARAM_BUILD_RUNTIME

    val buildNativeKey: String
        get() = DotnetConstants.PARAM_BUILD_NATIVE

    val buildCppKey: String
        get() = DotnetConstants.PARAM_BUILD_CPP

    val buildProfileKey: String
        get() = DotnetConstants.PARAM_BUILD_PROFILE

    val buildNonIncrementalKey: String
        get() = DotnetConstants.PARAM_BUILD_NON_INCREMENTAL

    val buildOutputKey: String
        get() = DotnetConstants.PARAM_BUILD_OUTPUT

    val buildTempKey: String
        get() = DotnetConstants.PARAM_BUILD_TEMP

    val publishFrameworkKey: String
        get() = DotnetConstants.PARAM_PUBLISH_FRAMEWORK

    val publishConfigKey: String
        get() = DotnetConstants.PARAM_PUBLISH_CONFIG

    val publishNativeKey: String
        get() = DotnetConstants.PARAM_PUBLISH_NATIVE

    val publishOutputKey: String
        get() = DotnetConstants.PARAM_PUBLISH_OUTPUT

    val publishRuntimeKey: String
        get() = DotnetConstants.PARAM_PUBLISH_RUNTIME

    val packBaseKey: String
        get() = DotnetConstants.PARAM_PACK_BASE

    val packConfigKey: String
        get() = DotnetConstants.PARAM_PACK_CONFIG

    val packOutputKey: String
        get() = DotnetConstants.PARAM_PACK_OUTPUT

    val packTempKey: String
        get() = DotnetConstants.PARAM_PACK_TEMP

    val packVersionSuffixKey: String
        get() = DotnetConstants.PARAM_PACK_VERSION_SUFFIX

    val verbosity: List<String>
        get() = listOf("Debug", "Verbose", "Information", "Warning", "Error")
}
