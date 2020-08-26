/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet

import jetbrains.buildServer.agent.Constants

/**
 * Dotnet runner constants.
 */
object DotnetConstants {
    const val RUNNER_TYPE = "dotnet"
    const val EXECUTABLE = "dotnet"
    const val DOTNET_DEFAULT_DIRECTORY = "dotnet"
    const val PROGRAM_FILES_ENV_VAR = "ProgramW6432"
    const val RUNNER_DISPLAY_NAME = ".NET"
    const val RUNNER_DESCRIPTION = "Provides .NET toolchain support for .NET projects"
    const val CLEANER_NAME = "$RUNNER_DISPLAY_NAME Cleaner"

    const val TOOL_HOME = "DOTNET_HOME"
    const val INTEGRATION_PACKAGE_HOME = "DOTNET_INTEGRATION_PACKAGE_HOME"
    const val PATH_SUFFIX = "_Path"
    const val CONFIG_NAME = "DotNetCLI"
    const val CONFIG_PATH = CONFIG_NAME + PATH_SUFFIX
    const val CONFIG_SDK_NAME = "DotNetCoreSDK"
    const val PARAM_DOCKER_IMAGE = "plugin.docker.imageId"

    // Internal configuration parameters:
    // True or False (False by default) - allows experimental features
    const val PARAM_EXPERIMENTAL = "teamcity.internal.dotnet.experimental"
    // True or False (False by default) - allows experimental features
    const val PARAM_SUPPORT_MSBUILD_BITNESS = "teamcity.internal.dotnet.msbuild.bitness"
    // On, MultiAdapterPath or Off (MultiAdapterPath by default)
    const val PARAM_TEST_REPORTING = "dotnet.cli.test.reporting"
    // True or False (True by default) - set to False to not use .rsp files
    const val PARAM_RSP = "dotnet.cli.rsp"
    // Semicolon separated list of variables to override FORCE_NUGET_EXE_INTERACTIVE;NUGET_HTTP_CACHE_PATH;NUGET_PACKAGES;NUGET_PLUGIN_PATHS;NUGET_RESTORE_MSBUILD_VERBOSITY (All by default), the empty string to not override at all - allows overriding NuGet environment variables
    const val PARAM_OVERRIDE_NUGET_VARS = "teamcity.internal.dotnet.override.nuget.vars"
    // Default bitness X86 or X64, X86 - if it is not specified
    const val PARAM_DEFAULT_BITNESS = "teamcity.internal.dotnet.default.bitness"

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
    const val PARAM_VSTEST_IN_ISOLATION = "vstest.InIsolation"
    const val PARAM_MSBUILD_LOGGER_PARAMS = "msbuild.logger.params"

    const val VALIDATION_EMPTY: String = "Should not be empty"

    // Tool providers
    const val PACKAGE_NUGET_EXTENSION = "nupkg"

    // Cross-platform dotCover
    const val DOTCOVER_PACKAGE_TYPE = "jetbrains.dotcover.dotnetclitool"
    const val DOTCOVER_WIN_PACKAGE_TYPE = "jetbrains.dotcover.commandlinetools"
    const val DOTCOVER_PACKAGE_TOOL_TYPE_NAME = "Cross-platform dotCover"
    const val DOTCOVER_PACKAGE_SHORT_TOOL_TYPE_NAME = "Cross-platform dotCover"
    const val DOTCOVER_PACKAGE_TARGET_FILE_DISPLAY_NAME = "DotCover Home Directory"
}
