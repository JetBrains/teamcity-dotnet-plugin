/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import org.jetbrains.annotations.NotNull;

/**
 * Provides parameters for dnx runner.
 */
public class DnxParametersProvider {

    @NotNull
    public String getCommandKey() {
        return DnxConstants.PARAM_COMMAND;
    }

    @NotNull
    public String getArgumentsKey() {
        return DnxConstants.PARAM_ARGUMENTS;
    }

    @NotNull
    public String getPathsKey() {
        return DnxConstants.PARAM_PATHS;
    }

    @NotNull
    public String getFrameworkKey() {
        return DnxConstants.PARAM_FRAMEWORK;
    }

    @NotNull
    public String getConfigKey() {
        return DnxConstants.PARAM_CONFIG;
    }

    @NotNull
    public String getAppbaseKey() {
        return DnxConstants.PARAM_APPBASE;
    }

    @NotNull
    public String getLibsKey() {
        return DnxConstants.PARAM_LIBS;
    }

    @NotNull
    public String getPackagesKey() {
        return DnxConstants.PARAM_PACKAGES;
    }
}
