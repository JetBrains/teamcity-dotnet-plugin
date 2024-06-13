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
    const val TEST_RETRY_FEATURE_NAME = "Test retry"
    const val PARALLEL_TESTS_FEATURE_NAME = "Parallel tests"
    const val TEST_CASE_FILTER_REQUIREMENTS_MESSAGE = "Test case filter, used by \"$TEST_RETRY_FEATURE_NAME\" and \"$PARALLEL_TESTS_FEATURE_NAME\" features, requires Microsoft.NET.Test.Sdk version 16.0.0 or newer"
    const val PARALLEL_TESTS_FEATURE_WITH_FILTER_REQUIREMENTS_MESSAGE = "The \"$PARALLEL_TESTS_FEATURE_NAME\" feature with a filter requires Microsoft.NET.Test.Sdk version 16.0.0 or newer"
    const val PARALLEL_TESTS_FEATURE_WITH_SUPPRESSION_REQUIREMENTS_MESSAGE = "The \"$PARALLEL_TESTS_FEATURE_NAME\" feature with test a pre-suppression requires Microsoft .NET SDK 6 or newer"

    const val TOOL_HOME = "DOTNET_HOME"
    const val INTEGRATION_PACKAGE_HOME = "DOTNET_INTEGRATION_PACKAGE_HOME"
    const val PARAM_DOCKER_IMAGE = "plugin.docker.imageId"

    // Internal configuration parameters:
    // True or False (False by default) - allows experimental features
    const val PARAM_EXPERIMENTAL = "teamcity.internal.dotnet.experimental"
    // True or False (False by default) - allows experimental features
    const val PARAM_SUPPORT_MSBUILD_BITNESS = "teamcity.internal.dotnet.msbuild.bitness"
    const val PARAM_MSBUILD_PARAMETERS_ESCAPE = "teamcity.internal.dotnet.msbuild.parameters.escape"
    const val PARAM_MSBUILD_DISABLE_TRAILING_BACKSLASH_QUOTATION = "teamcity.internal.dotnet.msbuild.parameters.disable.trailing.backslash.quotation"
    const val PARAM_MSBUILD_DISABLE_CUSTOM_VSTEST_LOGGERS = "teamcity.internal.dotnet.msbuild.parameters.disable.custom.vstest.loggers"
    // On, MultiAdapterPath or Off (MultiAdapterPath by default)
    const val PARAM_TEST_REPORTING = "dotnet.cli.test.reporting"
    // true or false (true by default) - enables a feature of adding an excluded target paths for `dotnet test` and `dotnet vstest` commands
    const val PARAM_TEST_EXCLUDED_PATHS_ENABLED = "teamcity.internal.dotnet.test.excludedPaths.enabled"
    // Semicolon separated list of variables to override FORCE_NUGET_EXE_INTERACTIVE;NUGET_HTTP_CACHE_PATH;NUGET_PACKAGES;NUGET_PLUGIN_PATHS;NUGET_RESTORE_MSBUILD_VERBOSITY (All by default), the empty string to not override at all - allows overriding NuGet environment variables
    const val PARAM_OVERRIDE_NUGET_VARS = "teamcity.internal.dotnet.override.nuget.vars"
    // Default bitness X86 or X64, X86 - if it is not specified
    const val PARAM_DEFAULT_BITNESS = "teamcity.internal.dotnet.default.bitness"
    // True or False (False by default) - use messages guard
    // TODO the guard was only used for test report service messages, remove it in a few releases after releasing filestreaming-based test reporting
    const val PARAM_MESSAGES_GUARD = "teamcity.internal.dotnet.messages.guard"
    const val PARAM_PARALLEL_TESTS_EXCLUDES_FILE = "teamcity.build.parallelTests.excludesFile"
    const val PARAM_PARALLEL_TESTS_INCLUDES_FILE = "teamcity.build.parallelTests.includesFile"
    const val PARAM_PARALLEL_TESTS_CURRENT_BATCH = "teamcity.build.parallelTests.currentBatch"
    // One of the following values: Trim (deprecated), NoProcessing (deprecated), EscapeSpecialCharacters (default).
    // Determines how to process test class parameters that are passed to the `dotnet test --filter <FILTER>` command
    const val PARAM_PARALLEL_TESTS_CLASS_PARAMETERS_PROCESSING_MODE = "teamcity.internal.dotnet.test.filter.classParametersProcessingMode"
    // True or False (False by default) - use exact match test filters for `dotnet test --filter <FILTER>` command in case of low performance reasoned by a huge amount of test classes via NUnit
    const val PARAM_PARALLEL_TESTS_USE_EXACT_MATCH_FILTER = "teamcity.internal.dotnet.test.exact.match.filter"
    // Integer amount of tests that should be included in exact match test filter
    const val PARAM_PARALLEL_TESTS_EXACT_MATCH_FILTER_SIZE = "teamcity.internal.dotnet.test.exact.match.filter.size"
    // True or False (False by default) - use test suppression strategy to split tests
    const val PARAM_PARALLEL_TESTS_USE_SUPPRESSION = "teamcity.internal.dotnet.test.suppression"
    // Integer minimum amount of test classes to activate test suppression strategy
    const val PARAM_PARALLEL_TESTS_SUPPRESSION_TEST_CLASSES_THRESHOLD = "teamcity.internal.dotnet.test.suppression.test.classes.threshold"
    // True or False (False by default) - report tests via stdout rather than via files
    const val PARAM_USE_STDOUT_TEST_REPORTING = "teamcity.internal.dotnet.test.reporting.useStdOut"
    // true or false (false by default) - dotCover coverage data post processing is enabled
    const val PARAM_DOTCOVER_COVERAGE_DATA_POST_PROCESSING_ENABLED = "teamcity.internal.dotnet.dotCover.wrapper.coverageDataPostProcessingEnabled"
    // true or false (true by default) - automatically merge all the snapshots produced by dotCover wrapper option is enabled
    const val PARAM_DOTCOVER_WRAPPER_MERGE_ENABLED = "teamcity.internal.dotnet.dotCover.wrapper.mergeEnabled"
    // true or false (true by default) - automatically generate report in the dotCover wrapper option is enabled
    const val PARAM_DOTCOVER_WRAPPER_REPORT_ENABLED = "teamcity.internal.dotnet.dotCover.wrapper.reportEnabled"
    // true or false (true by default) - add default exclude assembly filters for dotCover
    const val PARAM_DOTCOVER_WRAPPER_COVER_DEFAULT_ASSEMBLY_FILTERS_ENABLED = "teamcity.internal.dotnet.dotCover.wrapper.defaultExcludeAssemblyFiltersEnabled"
    // true or false (true by default) - add default exclude attribute filters for dotCover
    const val PARAM_DOTCOVER_WRAPPER_COVER_DEFAULT_ATTRIBUTE_FILTERS_ENABLED = "teamcity.internal.dotnet.dotCover.wrapper.defaultExcludeAttributeFiltersEnabled"
    // true or false (false by default) - is the overriding of TMP, TEMP, TMPDIR env vars with an empty string enabled for dotCover
    const val PARAM_DOTCOVER_OVERRIDING_TEMP_DIR_WITH_EMPTY_VALUE_ENABLED = "teamcity.internal.dotnet.dotCover.overridingTempDirWithEmptyValueEnabled"
    // true or false (true by default)
    const val PARAM_DOTCOVER_TEMP_DIR_OVERRIDE = "teamcity.internal.dotcover.temp.directory.override"
    // true or false (false by default)
    const val PARAM_TEST_RETRY_ENABLED = "teamcity.internal.dotnet.test.retry.enabled"
    // Integer maximum count of failed tests to retry
    const val PARAM_TEST_RETRY_MAX_FAILURES = "teamcity.internal.dotnet.test.retry.maxFailures"
    // Integer max NuGet cache clean time in seconds
    const val PARAM_NUGET_CACHE_CLEAN_TIMEOUT = "teamcity.internal.dotnet.nuget.cache.clean.timeoutSec"
    const val PARAM_NUGET_CACHE_CLEAN_IDLE_TIMEOUT_OVERRIDE = "teamcity.internal.dotnet.nuget.cache.clean.idleTimeoutOverrideEnabled"

    // Internal properties
    // true or false (true by default)
    const val PARAM_DOTCOVER_RUNNER_ENABLED = "teamcity.internal.dotnet.dotCover.runner.enabled"

    const val PARAM_ARGUMENTS = "args"
    const val PARAM_COMMAND = "command"
    const val PARAM_CONFIG = "configuration"
    const val PARAM_FRAMEWORK = "framework"
    const val PARAM_REQUIRED_SDK = "required.sdk"
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
    const val PARAM_EXCLUDED_PATHS = "excludedPaths"
    const val PARAM_PLATFORM = "platform"
    const val PARAM_RUNTIME = "runtime"
    const val PARAM_TARGETS = "targets"
    const val PARAM_TEST_FILTER = "test.filter"
    const val PARAM_TEST_NAMES = "test.names"
    const val PARAM_TEST_CASE_FILTER = "test.testCaseFilter"
    const val PARAM_TEST_SETTINGS_FILE = "test.settingsFile"
    const val PARAM_TEST_RETRY_MAX_RETRIES = "test.retry.maxRetries"
    const val PARAM_VISUAL_STUDIO_ACTION = "vs.action"
    const val PARAM_VISUAL_STUDIO_VERSION = "vs.version"
    const val PARAM_VERBOSITY = "verbosity"
    const val PARAM_VERSION_SUFFIX = "versionSuffix"
    const val PARAM_VSTEST_VERSION = "vstest.version"
    const val PARAM_VSTEST_IN_ISOLATION = "vstest.InIsolation"
    const val PARAM_MSBUILD_LOGGER_PARAMS = "msbuild.logger.params"
    const val PARAM_SINGLE_SESSION = "singleSession"

    const val VALIDATION_EMPTY: String = "Should not be empty"
    const val VALIDATION_INVALID_TEST_RETRY: String = "Invalid test retry count"

    // Tool providers
    const val PACKAGE_NUGET_EXTENSION = "nupkg"

    // Cross-platform dotCover
    const val DOTCOVER_PACKAGE_TYPE = "jetbrains.dotcover.dotnetclitool"
    const val DOTCOVER_WIN_PACKAGE_TYPE = "jetbrains.dotcover.commandlinetools"
    const val DOTCOVER_PACKAGE_TOOL_TYPE_NAME = "Cross-platform dotCover"
    const val DOTCOVER_PACKAGE_SHORT_TOOL_TYPE_NAME = "Cross-platform dotCover"
    const val DOTCOVER_PACKAGE_TARGET_FILE_DISPLAY_NAME = "DotCover Home Directory"

    // Requirements
    const val CONFIG_PREFIX_DOTNET_FRAMEWORK_SDK = "DotNetFrameworkSDK"
    const val CONFIG_PREFIX_DOTNET_FRAMEWORK_TARGETING_PACK = "DotNetFrameworkTargetingPack"
    const val CONFIG_PREFIX_CORE_SDK = "DotNetCoreSDK"
    const val CONFIG_PREFIX_CORE_RUNTIME = "DotNetCoreRuntime"
    const val CONFIG_PREFIX_DOTNET_FAMEWORK = "DotNetFramework"
    const val CONFIG_PREFIX_MSBUILD_TOOLS = "MSBuildTools"
    const val CONFIG_PREFIX_DOTNET_MSTEST = "teamcity.dotnet.mstest"
    const val CONFIG_PREFIX_DOTNET_VSTEST = "teamcity.dotnet.vstest"
    const val CONFIG_PREFIX_DOTNET_CREDENTIAL_PROVIDER = "DotNetCredentialProvider"
    const val CONFIG_PREFIX_VISUAL_STUDIO = "VS"
    const val CONFIG_SUFFIX_PATH = "_Path"
    const val CONFIG_SUFFIX_DOTNET_CLI = "DotNetCLI"
    const val CONFIG_SUFFIX_DOTNET_CLI_PATH = CONFIG_SUFFIX_DOTNET_CLI + CONFIG_SUFFIX_PATH
    const val CONFIG_PREFIX_DOTNET_WORKLOADS = "DotNetWorkloads"
}