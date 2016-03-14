/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

/**
 * File utilities
 */
public final class FileUtils {

    private static final Logger LOG = Logger.getInstance(FileUtils.class.getName());

    /**
     * Returns a first matching file in the list of directories.
     *
     * @param paths   list of directories.
     * @param pattern pattern.
     * @return first matching file.
     */
    @Nullable
    public static String findToolPath(@NotNull final List<String> paths, @NotNull final Pattern pattern) {
        for (String path : paths) {
            final File directory = new File(path);
            if (!directory.exists()) {
                LOG.debug("Ignoring non existing directory " + path);
                continue;
            }

            final File[] files = directory.listFiles();
            if (files == null) {
                LOG.debug("Ignoring empty directory " + path);
                continue;
            }

            for (File file : files) {
                final String absolutePath = file.getAbsolutePath();
                if (pattern.matcher(absolutePath).find()) {
                    return absolutePath;
                }
            }
        }

        return null;
    }
}
