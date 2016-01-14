/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * DNX runtime detector.
 */
public class DnxRuntimeDetector {

    private static final Logger LOG = Logger.getInstance(DnxRuntimeDetector.class.getName());
    private static final String NO_DNX_RUNTIMES_FOUND = "No DNX runtimes found.";

    @NotNull
    public Map<String, String> getRuntimes() {
        final String userHomePath = System.getProperty("user.home");

        final File dnxRuntimes = new File(userHomePath, ".dnx/runtimes");
        if (!dnxRuntimes.exists()) {
            LOG.info(NO_DNX_RUNTIMES_FOUND);
            return Collections.emptyMap();
        }

        LOG.info("Observing DNX runtimes directory " + dnxRuntimes.getAbsolutePath());
        final File[] files = dnxRuntimes.listFiles();
        if (files == null) {
            LOG.info(NO_DNX_RUNTIMES_FOUND);
            return Collections.emptyMap();
        }

        final Map<String, String> runtimes = new HashMap<>(files.length);
        for (File runtime : files) {
            if (!runtime.isDirectory()) {
                LOG.debug("Ignoring file %s" + runtime.getName());
                continue;
            }

            LOG.info(String.format("Found DNX runtime %s at %s", runtime.getName(), runtime.getAbsolutePath()));
            runtimes.put(runtime.getName(), runtime.getAbsolutePath());
        }

        return runtimes;
    }
}
