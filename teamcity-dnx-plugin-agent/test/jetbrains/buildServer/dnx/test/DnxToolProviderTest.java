/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.test;

import jetbrains.buildServer.agent.ToolProvider;
import jetbrains.buildServer.agent.impl.ToolProvidersRegistryImpl;
import jetbrains.buildServer.dnx.DnxToolProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Dmitry.Tretyakov
 *         Date: 1/15/2016
 *         Time: 12:50 AM
 */
public class DnxToolProviderTest {

    @Test
    public void testGetPath() throws Exception {
        final ToolProvider toolProvider = new DnxToolProvider(new ToolProvidersRegistryImpl());
        final String dnuPath = toolProvider.getPath("dnu");

        System.out.print(dnuPath);

        Assert.assertNotNull(dnuPath);
    }

    @Test
    public void testSupport() throws Exception {
        final ToolProvider toolProvider = new DnxToolProvider(new ToolProvidersRegistryImpl());
        Assert.assertTrue(toolProvider.supports("dnu"));
    }
}