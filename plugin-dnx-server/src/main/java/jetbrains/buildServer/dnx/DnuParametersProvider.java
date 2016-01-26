/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import jetbrains.buildServer.dnx.commands.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Provides parameters for dnu runner.
 */
public class DnuParametersProvider {

    private final List<CommandType> myTypes;

    public DnuParametersProvider() {
        myTypes = Arrays.asList(
                new DnuBuildCommandType(),
                new DnuPackCommandType(),
                new DnuPublishCommandType(),
                new DnuRestoreCommandType());
    }

    @NotNull
    public List<CommandType> getTypes() {
        return myTypes;
    }

    @NotNull
    public String getCommandKey() {
        return DnuConstants.DNU_PARAM_COMMAND;
    }

    @NotNull
    public String getArgumentsKey() {
        return DnuConstants.DNU_PARAM_ARGUMENTS;
    }

    @NotNull
    public String getRestorePathsKey() {
        return DnuConstants.DNU_PARAM_RESTORE_PATHS;
    }

    @NotNull
    public String getParallelExecutionKey() {
        return DnuConstants.DNU_PARAM_PARALLEL;
    }

    @NotNull
    public String getPackagePathsKey() {
        return DnuConstants.DNU_PARAM_PACKAGES_PATH;
    }

    @NotNull
    public String getBuildPathsKey() {
        return DnuConstants.DNU_PARAM_BUILD_PATHS;
    }

    @NotNull
    public String getBuildFrameworkKey() {
        return DnuConstants.DNU_PARAM_BUILD_FRAMEWORK;
    }

    @NotNull
    public String getBuildConfigKey() {
        return DnuConstants.DNU_PARAM_BUILD_CONFIG;
    }

    @NotNull
    public String getBuildOutputKey() {
        return DnuConstants.DNU_PARAM_BUILD_OUTPUT;
    }

    @NotNull
    public String getPublishPathsKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_PATHS;
    }

    @NotNull
    public String getPublishFrameworkKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_FRAMEWORK;
    }

    @NotNull
    public String getPublishConfigKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_CONFIG;
    }

    @NotNull
    public String getPublishNativeKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_NATIVE;
    }

    @NotNull
    public String getPublishCompileSourcesKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_COMPILE_SOURCE;
    }

    @NotNull
    public String getPublishIncludeSymbolsKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_INCLUDE_SYMBOLS;
    }

    @NotNull
    public String getPublishOutputKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_OUTPUT;
    }

    @NotNull
    public String getPublishRuntimeKey() {
        return DnuConstants.DNU_PARAM_PUBLISH_RUNTIME;
    }

    @NotNull
    public String getPackPathsKey() {
        return DnuConstants.DNU_PARAM_PACK_PATHS;
    }

    @NotNull
    public String getPackFrameworkKey() {
        return DnuConstants.DNU_PARAM_PACK_FRAMEWORK;
    }

    @NotNull
    public String getPackConfigKey() {
        return DnuConstants.DNU_PARAM_PACK_CONFIG;
    }

    @NotNull
    public String getPackOutputKey() {
        return DnuConstants.DNU_PARAM_PACK_OUTPUT;
    }
}
