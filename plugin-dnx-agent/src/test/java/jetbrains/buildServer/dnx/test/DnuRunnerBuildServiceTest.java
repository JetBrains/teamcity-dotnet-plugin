/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dnx.test;

import jetbrains.buildServer.dnx.*;
import jetbrains.buildServer.dnx.arguments.*;
import jetbrains.buildServer.util.CollectionsUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry.Tretyakov
 *         Date: 1/19/2016
 *         Time: 4:01 PM
 */
public class DnuRunnerBuildServiceTest {

    @Test(dataProvider = "testBuildArgumentsData")
    public void testBuildArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new DnuBuildArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @Test(dataProvider = "testRestoreArgumentsData")
    public void testRestoreArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new DnuRestoreArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @Test(dataProvider = "testPublishArgumentsData")
    public void testPublishArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new DnuPublishArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @Test(dataProvider = "testPackArgumentsData")
    public void testPackArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new DnuPackArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @DataProvider(name = "testBuildArgumentsData")
    public Object[][] testBuildArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DnuConstants.DNU_PARAM_BUILD_PATHS, "path/"),
                        Arrays.asList("build", "path/")},

                {CollectionsUtil.asMap(
                        DnuConstants.DNU_PARAM_BUILD_FRAMEWORK, "dnxcore50",
                        DnuConstants.DNU_PARAM_BUILD_CONFIG, "Release"),
                        Arrays.asList("build", "--framework", "dnxcore50", "--configuration", "Release")},

                {CollectionsUtil.asMap(
                        DnuConstants.DNU_PARAM_BUILD_OUTPUT, "output/",
                        DnuConstants.DNU_PARAM_ARGUMENTS, "--quiet"),
                        Arrays.asList("build", "--out", "output/", "--quiet")},
        };
    }

    @DataProvider(name = "testRestoreArgumentsData")
    public Object[][] testRestoreArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DnuConstants.DNU_PARAM_RESTORE_PATHS, "path/"),
                        Arrays.asList("restore", "path/")},

                {CollectionsUtil.asMap(
                        DnuConstants.DNU_PARAM_PACKAGES_PATH, "packages/",
                        DnuConstants.DNU_PARAM_PARALLEL, "false"),
                        Arrays.asList("restore", "--packages", "packages/")},

                {CollectionsUtil.asMap(DnuConstants.DNU_PARAM_PARALLEL, "true"),
                        Arrays.asList("restore", "--parallel")},
        };
    }

    @DataProvider(name = "testPublishArgumentsData")
    public Object[][] testPublishArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DnuConstants.DNU_PARAM_PUBLISH_PATHS, "path/"),
                        Arrays.asList("publish", "path/")},

                {CollectionsUtil.asMap(
                        DnuConstants.DNU_PARAM_PUBLISH_FRAMEWORK, "dotcore",
                        DnuConstants.DNU_PARAM_PUBLISH_CONFIG, "Release"),
                        Arrays.asList("publish", "--framework", "dotcore", "--configuration", "Release")},

                {CollectionsUtil.asMap(
                        DnuConstants.DNU_PARAM_PUBLISH_RUNTIME, "active",
                        DnuConstants.DNU_PARAM_PUBLISH_NATIVE, "true",
                        DnuConstants.DNU_PARAM_PUBLISH_INCLUDE_SYMBOLS, "true"),
                        Arrays.asList("publish", "--runtime", "active", "--native", "--include-symbols")},
        };
    }

    @DataProvider(name = "testPackArgumentsData")
    public Object[][] testPackArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DnuConstants.DNU_PARAM_PACK_PATHS, "path/"),
                        Arrays.asList("pack", "path/")},

                {CollectionsUtil.asMap(
                        DnuConstants.DNU_PARAM_PACK_FRAMEWORK, "dnxcore50",
                        DnuConstants.DNU_PARAM_PACK_CONFIG, "Release"),
                        Arrays.asList("pack", "--framework", "dnxcore50", "--configuration", "Release")},

                {CollectionsUtil.asMap(
                        DnuConstants.DNU_PARAM_PACK_OUTPUT, "output/",
                        DnuConstants.DNU_PARAM_ARGUMENTS, "--quiet"),
                        Arrays.asList("pack", "--out", "output/", "--quiet")},
        };
    }
}
