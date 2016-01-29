/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.test;

import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import jetbrains.buildServer.agent.ToolProvider;
import jetbrains.buildServer.agent.impl.ToolProvidersRegistryImpl;
import jetbrains.buildServer.dnx.DnxToolProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Dmitry.Tretyakov
 *         Date: 1/15/2016
 *         Time: 12:50 AM
 */
public class DnxToolProviderTest {

    @Test
    public void testGetPathForDnu() {
        final ToolProvider toolProvider = new DnxToolProvider(new ToolProvidersRegistryImpl());
        String dnuPath = null;
        ToolCannotBeFoundException exception = null;

        try {
            dnuPath = toolProvider.getPath("dnu");
        } catch (ToolCannotBeFoundException e) {
            exception = e;
        }

        final String userHome = System.getProperty("user.home");
        final File dnxDirectory = new File(userHome, ".dnx");

        if (dnxDirectory.exists()){
            Assert.assertNotNull(dnuPath, "DNU path should not be null");
            Assert.assertTrue(new File(dnuPath).exists(), "DNU should exists");
        } else {
            Assert.assertNotNull(exception, "Should be thrown dnu not found exception");
        }
    }

    @Test
    public void testDnuSupport() throws Exception {
        final ToolProvider toolProvider = new DnxToolProvider(new ToolProvidersRegistryImpl());
        Assert.assertTrue(toolProvider.supports("dNu"));
    }

    @Test
    public void testDnxSupport() throws Exception {
        final ToolProvider toolProvider = new DnxToolProvider(new ToolProvidersRegistryImpl());
        Assert.assertTrue(toolProvider.supports("dnX"));
    }
}