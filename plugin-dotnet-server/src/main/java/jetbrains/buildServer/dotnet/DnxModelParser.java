/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jetbrains.buildServer.dotnet.models.Project;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * Provides serialization capabilities.
 */
public class DnxModelParser {

    private final Gson myGson;

    public DnxModelParser(){
        final GsonBuilder builder = new GsonBuilder();
        myGson = builder.create();
    }

    @Nullable
    public Project getProjectModel(@Nullable Element element) {
        if (element == null || !element.isContentAvailable()) {
            return null;
        }

        Reader inputStream = null;

        try {
            inputStream = getInputStreamReader(element.getInputStream());
            final BufferedReader reader = new BufferedReader(inputStream);
            return myGson.fromJson(reader, Project.class);
        } catch (Exception e) {
            String message = "Failed to retrieve file for given path " + element.getFullName() + ": " + e.toString();
            Loggers.SERVER.infoAndDebugDetails(message, e);
        } finally {
            FileUtil.close(inputStream);
        }

        return null;
    }

    private static Reader getInputStreamReader(InputStream inputStream) throws IOException {
        inputStream.mark(3);
        int byte1 = inputStream.read();
        int byte2 = inputStream.read();
        if (byte1 == 0xFF && byte2 == 0xFE) {
            return new InputStreamReader(inputStream, "UTF-16LE");
        } else if (byte1 == 0xFF && byte2 == 0xFF) {
            return new InputStreamReader(inputStream, "UTF-16BE");
        } else {
            int byte3 = inputStream.read();
            if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF) {
                return new InputStreamReader(inputStream, "UTF-8");
            } else {
                inputStream.reset();
                return new InputStreamReader(inputStream);
            }
        }
    }
}
