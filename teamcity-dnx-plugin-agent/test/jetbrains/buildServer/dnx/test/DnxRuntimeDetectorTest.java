/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.test;

import jetbrains.buildServer.dnx.DnxRuntimeDetector;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Dmitry.Tretyakov
 *         Date: 1/15/2016
 *         Time: 12:50 AM
 */
public class DnxRuntimeDetectorTest {

    @Test
    public void testGetRuntimes() throws Exception {
        final DnxRuntimeDetector runtimeDetector = new DnxRuntimeDetector();
        final Map<String, String> runtimes = runtimeDetector.getRuntimes();

        Assert.assertNotNull(runtimes);
    }
}