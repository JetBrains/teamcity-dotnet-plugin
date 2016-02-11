/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import jetbrains.buildServer.dotnet.commands.CommandType;
import jetbrains.buildServer.dotnet.commands.Dotnet.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Provides parameters for dotnet runner.
 */
public class DotnetParametersProvider {

    private final List<CommandType> myTypes;

    public DotnetParametersProvider() {
        myTypes = Arrays.asList(
                new BuildCommandType(),
                new PackCommandType(),
                new PublishCommandType(),
                new RestoreCommandType(),
                new TestCommandType());
    }

    @NotNull
    public List<CommandType> getTypes() {
        return myTypes;
    }

    @NotNull
    public String getCommandKey() {
        return DotnetConstants.PARAM_COMMAND;
    }

    @NotNull
    public String getPathsKey() {
        return DotnetConstants.PARAM_PATHS;
    }

    @NotNull
    public String getArgumentsKey() {
        return DotnetConstants.PARAM_ARGUMENTS;
    }

    @NotNull
    public String getVerbosityKey() {
        return DotnetConstants.PARAM_VERBOSITY;
    }

    @NotNull
    public String getRestorePackagesKey() {
        return DotnetConstants.PARAM_RESTORE_PACKAGES;
    }

    @NotNull
    public String getRestoreParallelKey() {
        return DotnetConstants.PARAM_RESTORE_PARALLEL;
    }

    @NotNull
    public String getRestoreSourceKey() {
        return DotnetConstants.PARAM_RESTORE_SOURCE;
    }

    @NotNull
    public String getBuildFrameworkKey() {
        return DotnetConstants.PARAM_BUILD_FRAMEWORK;
    }

    @NotNull
    public String getBuildArchKey() {
        return DotnetConstants.PARAM_BUILD_ARCH;
    }

    @NotNull
    public String getBuildConfigKey() {
        return DotnetConstants.PARAM_BUILD_CONFIG;
    }

    @NotNull
    public String getBuildRuntimeKey() {
        return DotnetConstants.PARAM_BUILD_RUNTIME;
    }

    @NotNull
    public String getBuildNativeKey() {
        return DotnetConstants.PARAM_BUILD_NATIVE;
    }

    @NotNull
    public String getBuildCppKey() {
        return DotnetConstants.PARAM_BUILD_CPP;
    }

    @NotNull
    public String getBuildProfileKey() {
        return DotnetConstants.PARAM_BUILD_PROFILE;
    }

    @NotNull
    public String getBuildNonIncrementalKey() {
        return DotnetConstants.PARAM_BUILD_NON_INCREMENTAL;
    }

    @NotNull
    public String getBuildOutputKey() {
        return DotnetConstants.PARAM_BUILD_OUTPUT;
    }

    @NotNull
    public String getBuildTempKey() {
        return DotnetConstants.PARAM_BUILD_TEMP;
    }

    @NotNull
    public String getPublishFrameworkKey() {
        return DotnetConstants.PARAM_PUBLISH_FRAMEWORK;
    }

    @NotNull
    public String getPublishConfigKey() {
        return DotnetConstants.PARAM_PUBLISH_CONFIG;
    }

    @NotNull
    public String getPublishNativeKey() {
        return DotnetConstants.PARAM_PUBLISH_NATIVE;
    }

    @NotNull
    public String getPublishOutputKey() {
        return DotnetConstants.PARAM_PUBLISH_OUTPUT;
    }

    @NotNull
    public String getPublishRuntimeKey() {
        return DotnetConstants.PARAM_PUBLISH_RUNTIME;
    }

    @NotNull
    public String getPackBaseKey() {
        return DotnetConstants.PARAM_PACK_BASE;
    }

    @NotNull
    public String getPackConfigKey() {
        return DotnetConstants.PARAM_PACK_CONFIG;
    }

    @NotNull
    public String getPackOutputKey() {
        return DotnetConstants.PARAM_PACK_OUTPUT;
    }

    @NotNull
    public String getPackTempKey() {
        return DotnetConstants.PARAM_PACK_TEMP;
    }

    @NotNull
    public String getPackVersionSuffixKey() {
        return DotnetConstants.PARAM_PACK_VERSION_SUFFIX;
    }

    @NotNull
    public List<String> getVerbosity() {
        return Arrays.asList("Debug", "Verbose", "Information", "Warning", "Error");
    }
}
