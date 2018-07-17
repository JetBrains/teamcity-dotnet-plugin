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
    const val RUNNER_TYPE = "dotnet.cli"
    const val EXECUTABLE = "dotnet"
    const val RUNNER_DISPLAY_NAME = ".NET CLI (dotnet)"
    const val RUNNER_DESCRIPTION = "Provides .NET CLI toolchain support for .NET projects"

    const val TOOL_HOME = "DOTNET_HOME"
    const val INTEGRATION_PACKAGE_HOME = "DOTNET_INTEGRATION_PACKAGE_HOME"
    const val PATH_SUFFIX = "_Path"
    const val CONFIG_NAME = "DotNetCLI"
    const val CONFIG_PATH = CONFIG_NAME + PATH_SUFFIX
    const val CONFIG_SDK_NAME = "DotNetCoreSDK"
    const val PARAM_DOCKER_IMAGE = "plugin.docker.imageId"

    const val PARAM_EXPERIMENTAL = "teamcity.dotnet.cli.experimental"
    const val PARAM_TEST_REPORTING = "dotnet.cli.test.reporting"
    // Set to false to not use .rsp files
    const val PARAM_RSP = "dotnet.cli.rsp"

    const val PARAM_ARGUMENTS = "args"
    const val PARAM_COMMAND = "command"
    const val PARAM_CONFIG = "configuration"
    const val PARAM_FRAMEWORK = "framework"
    const val PARAM_MSBUILD_VERSION = "msbuild.version"
    const val PARAM_NUGET_API_KEY = Constants.SECURE_PROPERTY_PREFIX + "nuget.apiKey"
    const val PARAM_NUGET_PACKAGE_ID = "nuget.packageId"
    const val PARAM_NUGET_PACKAGE_SOURCE = "nuget.packageSource"
    const val PARAM_NUGET_PACKAGE_SOURCES = "nuget.packageSources"
    const val PARAM_NUGET_PACKAGES_DIR = "nuget.packagesDir"
    const val PARAM_NUGET_NO_SYMBOLS = "nuget.noSymbols"
    const val PARAM_NUGET_CONFIG_FILE = "nuget.configFile"
    const val PARAM_SKIP_BUILD = "skipBuild"
    const val PARAM_OUTPUT_DIR = "outputDir"
    const val PARAM_PATHS = "paths"
    const val PARAM_PLATFORM = "platform"
    const val PARAM_RUNTIME = "runtime"
    const val PARAM_TARGETS = "targets"
    const val PARAM_TEST_FILTER = "test.filter"
    const val PARAM_TEST_NAMES = "test.names"
    const val PARAM_TEST_CASE_FILTER = "test.testCaseFilter"
    const val PARAM_TEST_SETTINGS_FILE = "test.settingsFile"
    const val PARAM_VISUAL_STUDIO_ACTION = "vs.action"
    const val PARAM_VISUAL_STUDIO_VERSION = "vs.version"
    const val PARAM_VERBOSITY = "verbosity"
    const val PARAM_VERSION_SUFFIX = "versionSuffix"
    const val PARAM_VSTEST_VERSION = "vstest.version"

    const val VALIDATION_EMPTY: String = "Should not be empty"

    // Integration package
    const val PACKAGE_NUGET_EXTENSION = "nupkg"
    const val PACKAGE_TYPE = "TeamCity.Dotnet.Integration"
    const val PACKAGE_TOOL_TYPE_NAME = "Dotnet Integration"
    const val PACKAGE_SHORT_TOOL_TYPE_NAME = "Dotnet Integration"
    const val PACKAGE_TARGET_FILE_DISPLAY_NAME = "Dotnet Integration Home Directory"
    const val PACKAGE_BINARY_NUPKG_PATH = "build/_common"
}
