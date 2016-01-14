/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx;

import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * DNX runner discovery extension.
 */
public class DnxRunnerDiscoveryExtension extends BreadthFirstRunnerDiscoveryExtension {

    @NotNull
    @Override
    protected List<DiscoveredObject> discoverRunnersInDirectory(@NotNull Element element, @NotNull List<Element> list) {
        return Collections.emptyList();
    }
}
