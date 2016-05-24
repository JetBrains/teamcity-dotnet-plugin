/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

/**
 * Dotnet runner constants.
 */
public interface DotnetConstants {
    String RUNNER_TYPE = "dotnet";
    String RUNNER_DISPLAY_NAME = ".NET Core (dotnet)";
    String RUNNER_DESCRIPTION = "Provides build tools for .NET Core projects";

    String TOOL_HOME = "DOTNET_HOME";
    String CONFIG_PATH = RUNNER_TYPE + "_Path";
    String PROJECT_JSON = "project.json";

    String COMMAND_BUILD = "build";
    String COMMAND_PACK = "pack";
    String COMMAND_PUBLISH = "publish";
    String COMMAND_RESTORE = "restore";
    String COMMAND_TEST = "test";

    String PARAM_COMMAND = "dotnet-command";
    String PARAM_PATHS = "dotnet-paths";
    String PARAM_ARGUMENTS = "dotnet-args";
    String PARAM_VERBOSITY = "dotnet-verbosity";

    String PARAM_BUILD_FRAMEWORK = "dotnet-build-framework";
    String PARAM_BUILD_ARCH = "dotnet-build-arch";
    String PARAM_BUILD_CONFIG = "dotnet-build-config";
    String PARAM_BUILD_RUNTIME = "dotnet-build-runtime";
    String PARAM_BUILD_NATIVE = "dotnet-build-native";
    String PARAM_BUILD_CPP = "dotnet-build-cpp";
    String PARAM_BUILD_PROFILE = "dotnet-build-profile";
    String PARAM_BUILD_NON_INCREMENTAL = "dotnet-build-not-incremental";
    String PARAM_BUILD_OUTPUT = "dotnet-build-output";
    String PARAM_BUILD_TEMP = "dotnet-build-temp";

    String PARAM_RESTORE_PARALLEL = "dotnet-restore-parallel";
    String PARAM_RESTORE_PACKAGES = "dotnet-restore-packages";
    String PARAM_RESTORE_SOURCE = "dotnet-restore-source";

    String PARAM_PUBLISH_FRAMEWORK = "dotnet-publish-framework";
    String PARAM_PUBLISH_CONFIG = "dotnet-publish-config";
    String PARAM_PUBLISH_NATIVE = "dotnet-publish-native";
    String PARAM_PUBLISH_OUTPUT = "dotnet-publish-output";
    String PARAM_PUBLISH_RUNTIME = "dotnet-publish-runtime";

    String PARAM_PACK_BASE = "dotnet-pack-base";
    String PARAM_PACK_CONFIG = "dotnet-pack-config";
    String PARAM_PACK_OUTPUT = "dotnet-pack-output";
    String PARAM_PACK_TEMP = "dotnet-pack-temp";
    String PARAM_PACK_VERSION_SUFFIX = "dotnet-pack-version-suffix";
}
