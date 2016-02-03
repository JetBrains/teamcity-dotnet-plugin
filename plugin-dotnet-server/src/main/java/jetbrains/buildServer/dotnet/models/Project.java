/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.models;

import java.util.Map;

/**
 * Represents dnx project model.
 */
public class Project {
    public Map<String, String> commands;
    public Map<String, Object> configurations;
    public Map<String, Object> frameworks;
}
