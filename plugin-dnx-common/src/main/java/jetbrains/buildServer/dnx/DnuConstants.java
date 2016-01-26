/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

/**
 * Dnu runner constants.
 */
public interface DnuConstants {
    String DNU_RUNNER_TYPE = "dnu";
    String DNU_RUNNER_DISPLAY_NAME = "dnu";
    String DNU_RUNNER_DESCRIPTION = "Provides DNX package management";

    String DNU_COMMAND_BUILD = "build";
    String DNU_COMMAND_PUBLISH = "publish";
    String DNU_COMMAND_RESTORE = "restore";

    String DNU_PARAM_COMMAND = "dnu-command";
    String DNU_PARAM_ARGUMENTS = "dnu-args";
    String DNU_PARAM_RESTORE_PATHS = "dnu-restore-paths";
    String DNU_PARAM_PARALLEL = "dnu-parallel";
    String DNU_PARAM_PACKAGES_PATH = "dnu-packages-path";

    String DNU_PARAM_BUILD_PATHS = "dnu-build-paths";
    String DNU_PARAM_BUILD_FRAMEWORK = "dnu-build-framework";
    String DNU_PARAM_BUILD_CONFIG = "dnu-build-config";
    String DNU_PARAM_BUILD_OUTPUT = "dnu-build-output";

    String DNU_PARAM_PUBLISH_PATHS = "dnu-publish-paths";
    String DNU_PARAM_PUBLISH_FRAMEWORK = "dnu-publish-framework";
    String DNU_PARAM_PUBLISH_CONFIG = "dnu-publish-config";
    String DNU_PARAM_PUBLISH_NATIVE = "dnu-publish-native";
    String DNU_PARAM_PUBLISH_COMPILE_SOURCE = "dnu-publish-compile-sources";
    String DNU_PARAM_PUBLISH_INCLUDE_SYMBOLS = "dnu-publish-include-symbols";
    String DNU_PARAM_PUBLISH_OUTPUT = "dnu-publish-output";
    String DNU_PARAM_PUBLISH_RUNTIME = "dnu-publish-runtime";
}
