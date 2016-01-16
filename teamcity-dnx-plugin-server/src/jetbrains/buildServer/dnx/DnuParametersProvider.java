/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import org.jetbrains.annotations.NotNull;

/**
 * Provides parameters for DNX utility runner.
 */
public class DnuParametersProvider {

    @NotNull
    public String[] getCommands() {
        return new String[]{"restore"};
    }

    @NotNull
    public String getCommandKey() {
        return DnuConstants.DNU_PARAM_COMMAND;
    }

    @NotNull
    public String getProjectPathsKey() {
        return DnuConstants.DNU_PARAM_PROJECTS;
    }

    @NotNull
    public String getArgumentsKey() {
        return DnuConstants.DNU_PARAM_ARGUMENTS;
    }

    @NotNull
    public String getParallelExecutionKey() {
        return DnuConstants.DNU_PARAM_PARALLEL;
    }

    @NotNull
    public String getPackagePathsKey() {
        return DnuConstants.DNU_PARAM_PACKAGES;
    }
}
