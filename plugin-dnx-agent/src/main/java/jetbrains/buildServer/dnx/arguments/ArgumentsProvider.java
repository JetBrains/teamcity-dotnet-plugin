/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.arguments;

import java.util.List;
import java.util.Map;

/**
 * Provides arguments to the utility.
 */
public interface ArgumentsProvider {
    List<String> getArguments(final Map<String, String> parameters);
}
