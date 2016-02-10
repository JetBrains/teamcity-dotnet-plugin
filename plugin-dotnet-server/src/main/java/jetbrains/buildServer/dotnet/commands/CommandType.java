/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.commands;

import org.jetbrains.annotations.NotNull;

/**
 * Provides command-specific resources.
 */
public interface CommandType {
    @NotNull
    String getName();

    @NotNull
    String getEditPage();

    @NotNull
    String getViewPage();
}
