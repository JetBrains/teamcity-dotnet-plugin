/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

/**
 * Dotnet runner constants.
 */
class DotnetConstants {
    companion object {
        val RUNNER_TYPE = "dotnet"
        val RUNNER_DISPLAY_NAME = ".NET Core (dotnet)"
        val RUNNER_DESCRIPTION = "Provides build tools for .NET Core projects"

        val TOOL_HOME = "DOTNET_HOME"
        val CONFIG_PATH = RUNNER_TYPE + "_Path"
        val PROJECT_JSON = "project.json"

        val COMMAND_BUILD = "build"
        val COMMAND_PACK = "pack"
        val COMMAND_PUBLISH = "publish"
        val COMMAND_RESTORE = "restore"
        val COMMAND_TEST = "test"

        val PARAM_COMMAND = "dotnet-command"
        val PARAM_PATHS = "dotnet-paths"
        val PARAM_ARGUMENTS = "dotnet-args"
        val PARAM_VERBOSITY = "dotnet-verbosity"

        val PARAM_BUILD_FRAMEWORK = "dotnet-build-framework"
        val PARAM_BUILD_CONFIG = "dotnet-build-config"
        val PARAM_BUILD_RUNTIME = "dotnet-build-runtime"
        val PARAM_BUILD_PROFILE = "dotnet-build-profile"
        val PARAM_BUILD_NON_INCREMENTAL = "dotnet-build-not-incremental"
        val PARAM_BUILD_NO_DEPENDENCIES = "dotnet-build-no-deps"
        val PARAM_BUILD_OUTPUT = "dotnet-build-output"
        val PARAM_BUILD_TEMP = "dotnet-build-temp"

        val PARAM_RESTORE_PARALLEL = "dotnet-restore-parallel"
        val PARAM_RESTORE_PACKAGES = "dotnet-restore-packages"
        val PARAM_RESTORE_SOURCE = "dotnet-restore-source"
        val PARAM_RESTORE_CONFIG = "dotnet-restore-config"

        val PARAM_PUBLISH_FRAMEWORK = "dotnet-publish-framework"
        val PARAM_PUBLISH_CONFIG = "dotnet-publish-config"
        val PARAM_PUBLISH_OUTPUT = "dotnet-publish-output"
        val PARAM_PUBLISH_TEMP = "dotnet-publish-temp"
        val PARAM_PUBLISH_RUNTIME = "dotnet-publish-runtime"
        val PARAM_PUBLISH_VERSION_SUFFIX = "dotnet-publish-version-suffix"
        val PARAM_PUBLISH_NO_BUILD = "dotnet-publish-no-build"

        val PARAM_PACK_CONFIG = "dotnet-pack-config"
        val PARAM_PACK_OUTPUT = "dotnet-pack-output"
        val PARAM_PACK_TEMP = "dotnet-pack-temp"
        val PARAM_PACK_VERSION_SUFFIX = "dotnet-pack-version-suffix"
        val PARAM_PACK_NO_BUILD = "dotnet-pack-no-build"
        val PARAM_PACK_SERVICEABLE = "dotnet-pack-serviceable"
    }
}
