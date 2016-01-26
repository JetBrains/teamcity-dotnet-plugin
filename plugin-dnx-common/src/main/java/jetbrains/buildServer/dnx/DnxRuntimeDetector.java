/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DNX runtime detector.
 */
public class DnxRuntimeDetector {

    public Map<String, String> getRuntimes() {
        final String userHomePath = System.getProperty("user.home");

        final File dnxRuntimes = new File(userHomePath, ".dnx/runtimes");
        if (!dnxRuntimes.exists()) {
            return Collections.emptyMap();
        }

        final File[] files = dnxRuntimes.listFiles();
        if (files == null) {
            return Collections.emptyMap();
        }

        final Map<String, String> runtimes = new HashMap<String, String>(files.length);
        for (File runtime : files) {
            if (!runtime.isDirectory()) {
                continue;
            }

            runtimes.put(runtime.getName(), runtime.getAbsolutePath());
        }

        return runtimes;
    }
}
