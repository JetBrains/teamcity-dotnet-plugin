/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Constants

/**
 * Dotnet runner constants.
 */
object DotnetConstants {
    const val RUNNER_TYPE = "dotnet"
    const val RUNNER_DISPLAY_NAME = ".NET Core (dotnet)"
    const val RUNNER_DESCRIPTION = "Provides build tools for .NET Core projects"

    const val TOOL_HOME = "DOTNET_HOME"
    const val INTEGRATION_PACKAGE_HOME = "DOTNET_INTEGRATION_PACKAGE_HOME"
    const val CONFIG_NAME = "DotNetCore"
    const val CONFIG_PATH = CONFIG_NAME + "_Path"
    const val PROJECT_JSON = "project.json"
    const val PROJECT_CSPROJ = ".csproj"
    const val PROJECT_SLN = ".sln"

    const val PARAM_COMMAND = "dotnet-command"
    const val PARAM_PATHS = "dotnet-paths"
    const val PARAM_ARGUMENTS = "dotnet-args"
    const val PARAM_VERBOSITY = "dotnet-verbosity"

    const val PARAM_BUILD_FRAMEWORK = "dotnet-build-framework"
    const val PARAM_BUILD_CONFIG = "dotnet-build-config"
    const val PARAM_BUILD_RUNTIME = "dotnet-build-runtime"
    const val PARAM_BUILD_NON_INCREMENTAL = "dotnet-build-not-incremental"
    const val PARAM_BUILD_NO_DEPENDENCIES = "dotnet-build-no-deps"
    const val PARAM_BUILD_OUTPUT = "dotnet-build-output"
    const val PARAM_BUILD_VERSION_SUFFIX = "dotnet-build-version-suffix"

    const val PARAM_RESTORE_PARALLEL = "dotnet-restore-parallel"
    const val PARAM_RESTORE_PACKAGES = "dotnet-restore-packages"
    const val PARAM_RESTORE_SOURCE = "dotnet-restore-source"
    const val PARAM_RESTORE_CONFIG = "dotnet-restore-config"
    const val PARAM_RESTORE_NO_CACHE = "dotnet-restore-no-cache"
    const val PARAM_RESTORE_IGNORE_FAILED = "dotnet-restore-ignore-failed"
    const val PARAM_RESTORE_ROOT_PROJECT = "dotnet-restore-root-project"

    const val PARAM_PUBLISH_FRAMEWORK = "dotnet-publish-framework"
    const val PARAM_PUBLISH_CONFIG = "dotnet-publish-config"
    const val PARAM_PUBLISH_OUTPUT = "dotnet-publish-output"
    const val PARAM_PUBLISH_RUNTIME = "dotnet-publish-runtime"
    const val PARAM_PUBLISH_VERSION_SUFFIX = "dotnet-publish-version-suffix"

    const val PARAM_PACK_CONFIG = "dotnet-pack-config"
    const val PARAM_PACK_OUTPUT = "dotnet-pack-output"
    const val PARAM_PACK_TEMP = "dotnet-pack-temp"
    const val PARAM_PACK_VERSION_SUFFIX = "dotnet-pack-version-suffix"
    const val PARAM_PACK_NO_BUILD = "dotnet-pack-no-build"
    const val PARAM_PACK_SERVICEABLE = "dotnet-pack-serviceable"

    const val PARAM_TEST_FRAMEWORK = "dotnet-test-framework"
    const val PARAM_TEST_CONFIG = "dotnet-test-config"
    const val PARAM_TEST_OUTPUT = "dotnet-test-output"
    const val PARAM_TEST_TEMP = "dotnet-test-temp"
    const val PARAM_TEST_RUNTIME = "dotnet-test-runtime"
    const val PARAM_TEST_NO_BUILD = "dotnet-test-no-build"

    const val PARAM_RUN_FRAMEWORK = "dotnet-run-framework"
    const val PARAM_RUN_CONFIG = "dotnet-run-config"

    const val PARAM_NUGET_PUSH_API_KEY = Constants.SECURE_PROPERTY_PREFIX + "dotnet-nuget-push-api-key"
    const val PARAM_NUGET_PUSH_SOURCE = "dotnet-nuget-push-source"
    const val PARAM_NUGET_PUSH_NO_BUFFER = "dotnet-nuget-push-no-buffer"
    const val PARAM_NUGET_PUSH_NO_SYMBOLS = "dotnet-nuget-push-no-symbols"
    const val PARAM_NUGET_DELETE_ID = "dotnet-nuget-delete-id"
    const val PARAM_NUGET_DELETE_API_KEY = Constants.SECURE_PROPERTY_PREFIX + "dotnet-nuget-delete-api-key"
    const val PARAM_NUGET_DELETE_SOURCE = "dotnet-nuget-push-source"

    const val PARAM_CLEAN_FRAMEWORK = "dotnet-clean-framework"
    const val PARAM_CLEAN_CONFIG = "dotnet-clean-config"
    const val PARAM_CLEAN_RUNTIME = "dotnet-clean-runtime"
    const val PARAM_CLEAN_OUTPUT = "dotnet-clean-output"

    const val PARAM_MSBUILD_TARGETS = "dotnet-msbuild-targets"
    const val PARAM_MSBUILD_CONFIG = "dotnet-msbuild-config"
    const val PARAM_MSBUILD_PLATFORM = "dotnet-msbuild-platform"

    const val VALIDATION_EMPTY: String = "Should not be empty"

    // Integration package
    const val PACKAGE_FEED_URL = "http://packages.nuget.org/api/v2"
    const val PACKAGE_NUGET_EXTENSION = ".nupkg"
    const val PACKAGE_TYPE = "TeamCity.Dotnet.Integration"
    const val PACKAGE_TOOL_TYPE_NAME = "Dotnet Integration"
    const val PACKAGE_SHORT_TOOL_TYPE_NAME = "Dotnet Integration"
    const val PACKAGE_TARGET_FILE_DISPLAY_NAME = "Dotnet Integration Home Directory"
    const val PACKAGE_BINARY_NUPKG_PATH = "build/_common"
}
