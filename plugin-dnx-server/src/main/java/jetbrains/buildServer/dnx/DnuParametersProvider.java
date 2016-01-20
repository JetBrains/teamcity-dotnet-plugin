/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Provides parameters for DNX utility runner.
 */
public class DnuParametersProvider {

    private final List<CommandType> myTypes;

    public DnuParametersProvider() {
        myTypes = Arrays.asList(new DnuBuildCommandType(), new DnuRestoreCommandType());
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
    public List<CommandType> getTypes() {
        return myTypes;
    }
}
