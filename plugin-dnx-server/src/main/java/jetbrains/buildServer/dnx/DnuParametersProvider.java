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
        return DnuConstants.PARAM_COMMAND;
    }

    @NotNull
    public String getArgumentsKey() {
        return DnuConstants.PARAM_ARGUMENTS;
    }

    @NotNull
    public String getPathsKey() {
        return DnuConstants.PARAM_PATHS;
    }

    @NotNull
    public String getRestorePackagesKey() {
        return DnuConstants.PARAM_RESTORE_PACKAGES;
    }

    @NotNull
    public String getRestoreParallelKey() {
        return DnuConstants.PARAM_RESTORE_PARALLEL;
    }

    @NotNull
    public String getBuildFrameworkKey() {
        return DnuConstants.PARAM_BUILD_FRAMEWORK;
    }

    @NotNull
    public String getBuildConfigKey() {
        return DnuConstants.PARAM_BUILD_CONFIG;
    }

    @NotNull
    public String getBuildOutputKey() {
        return DnuConstants.PARAM_BUILD_OUTPUT;
    }

    @NotNull
    public String getPublishFrameworkKey() {
        return DnuConstants.PARAM_PUBLISH_FRAMEWORK;
    }

    @NotNull
    public String getPublishConfigKey() {
        return DnuConstants.PARAM_PUBLISH_CONFIG;
    }

    @NotNull
    public String getPublishNativeKey() {
        return DnuConstants.PARAM_PUBLISH_NATIVE;
    }

    @NotNull
    public String getPublishCompileSourcesKey() {
        return DnuConstants.PARAM_PUBLISH_COMPILE_SOURCE;
    }

    @NotNull
    public String getPublishIncludeSymbolsKey() {
        return DnuConstants.PARAM_PUBLISH_INCLUDE_SYMBOLS;
    }

    @NotNull
    public String getPublishOutputKey() {
        return DnuConstants.PARAM_PUBLISH_OUTPUT;
    }

    @NotNull
    public String getPublishRuntimeKey() {
        return DnuConstants.PARAM_PUBLISH_RUNTIME;
    }

    @NotNull
    public String getPackFrameworkKey() {
        return DnuConstants.PARAM_PACK_FRAMEWORK;
    }

    @NotNull
    public String getPackConfigKey() {
        return DnuConstants.PARAM_PACK_CONFIG;
    }

    @NotNull
    public String getPackOutputKey() {
        return DnuConstants.PARAM_PACK_OUTPUT;
    }
}
