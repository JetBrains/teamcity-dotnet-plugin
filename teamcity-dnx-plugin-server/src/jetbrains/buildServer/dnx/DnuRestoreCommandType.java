/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import jetbrains.buildServer.requirements.Requirement;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Provides paramenters for dnu restore command.
 */
public class DnuRestoreCommandType implements CommandType {
    @NotNull
    @Override
    public String getName() {
        return DnuConstants.DNU_COMMAND_RESTORE;
    }

    @NotNull
    @Override
    public String getEditPage() {
        return "editDnuRestoreParameters.jsp";
    }

    @NotNull
    @Override
    public String getViewPage() {
        return "viewDnuRestoreParameters.jsp";
    }

    @NotNull
    @Override
    public Map<String, String> getDefaultParameters() {
        return null;
    }

    @NotNull
    @Override
    public List<Requirement> getRunnerSpecificRequirements(@NotNull Map<String, String> runParameters) {
        return null;
    }

    @Nullable
    @Override
    public PropertiesProcessor getPropertiesProcessor() {
        return null;
    }
}
