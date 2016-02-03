/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands;

import jetbrains.buildServer.dotnet.DnuConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Provides parameters for dnu restore command.
 */
public class DnuRestoreCommandType implements CommandType {
    @NotNull
    @Override
    public String getName() {
        return DnuConstants.COMMAND_RESTORE;
    }

    @NotNull
    @Override
    public String getEditPage() {
        return "editRestoreParameters.jsp";
    }

    @NotNull
    @Override
    public String getViewPage() {
        return "viewRestoreParameters.jsp";
    }
}
