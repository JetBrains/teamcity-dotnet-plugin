/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

/**
 * Dnu runner constants.
 */
public interface DnuConstants {
    String RUNNER_TYPE = "dnu";
    String RUNNER_DISPLAY_NAME = ".NET Core (dnu) (retired)";
    String RUNNER_DESCRIPTION = "Provides DNX package management";

    String COMMAND_BUILD = "build";
    String COMMAND_PACK = "pack";
    String COMMAND_PUBLISH = "publish";
    String COMMAND_RESTORE = "restore";

    String PARAM_COMMAND = "dnu-command";
    String PARAM_ARGUMENTS = "dnu-args";
    String PARAM_PATHS = "dnu-paths";

    String PARAM_RESTORE_PARALLEL = "dnu-restore-parallel";
    String PARAM_RESTORE_PACKAGES = "dnu-restore-packages";

    String PARAM_BUILD_FRAMEWORK = "dnu-build-framework";
    String PARAM_BUILD_CONFIG = "dnu-build-config";
    String PARAM_BUILD_OUTPUT = "dnu-build-output";

    String PARAM_PUBLISH_FRAMEWORK = "dnu-publish-framework";
    String PARAM_PUBLISH_CONFIG = "dnu-publish-config";
    String PARAM_PUBLISH_NATIVE = "dnu-publish-native";
    String PARAM_PUBLISH_COMPILE_SOURCE = "dnu-publish-compile-sources";
    String PARAM_PUBLISH_INCLUDE_SYMBOLS = "dnu-publish-include-symbols";
    String PARAM_PUBLISH_OUTPUT = "dnu-publish-output";
    String PARAM_PUBLISH_RUNTIME = "dnu-publish-runtime";

    String PARAM_PACK_FRAMEWORK = "dnu-pack-framework";
    String PARAM_PACK_CONFIG = "dnu-pack-config";
    String PARAM_PACK_OUTPUT = "dnu-pack-output";
}
