/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test;

import jetbrains.buildServer.agent.ToolProvider;
import jetbrains.buildServer.agent.impl.ToolProvidersRegistryImpl;
import jetbrains.buildServer.dotnet.DotnetToolProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Dmitry.Tretyakov
 *         Date: 1/15/2016
 *         Time: 12:50 AM
 */
public class DotnetToolProviderTest {

    @Test
    public void testDotnetSupport() throws Exception {
        final ToolProvider toolProvider = new DotnetToolProvider(new ToolProvidersRegistryImpl());
        Assert.assertTrue(toolProvider.supports("dOtNeT"));
    }
}