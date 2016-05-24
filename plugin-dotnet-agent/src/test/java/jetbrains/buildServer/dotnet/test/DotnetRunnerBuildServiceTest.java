/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test;

import jetbrains.buildServer.dotnet.*;
import jetbrains.buildServer.dotnet.dotnet.BuildArgumentsProvider;
import jetbrains.buildServer.dotnet.dotnet.PackArgumentsProvider;
import jetbrains.buildServer.dotnet.dotnet.PublishArgumentsProvider;
import jetbrains.buildServer.dotnet.dotnet.RestoreArgumentsProvider;
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
public class DotnetRunnerBuildServiceTest {

    @Test(dataProvider = "testBuildArgumentsData")
    public void testBuildArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new BuildArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @Test(dataProvider = "testRestoreArgumentsData")
    public void testRestoreArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new RestoreArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @Test(dataProvider = "testPublishArgumentsData")
    public void testPublishArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new PublishArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @Test(dataProvider = "testPackArgumentsData")
    public void testPackArguments(final Map<String, String> parameters, final List<String> arguments) {
        final ArgumentsProvider argumentsProvider = new PackArgumentsProvider();
        final List<String> result = argumentsProvider.getArguments(parameters);

        Assert.assertEquals(result, arguments);
    }

    @DataProvider(name = "testBuildArgumentsData")
    public Object[][] testBuildArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DotnetConstants.PARAM_PATHS, "path/"),
                        Arrays.asList("build", "path/")},

                {CollectionsUtil.asMap(
                        DotnetConstants.PARAM_BUILD_FRAMEWORK, "dnxcore50",
                        DotnetConstants.PARAM_BUILD_CONFIG, "Release"),
                        Arrays.asList("build", "--framework", "dnxcore50", "--configuration", "Release")},

                {CollectionsUtil.asMap(
                        DotnetConstants.PARAM_BUILD_OUTPUT, "output/",
                        DotnetConstants.PARAM_ARGUMENTS, "--quiet"),
                        Arrays.asList("build", "--output", "output/", "--quiet")},
        };
    }

    @DataProvider(name = "testRestoreArgumentsData")
    public Object[][] testRestoreArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DotnetConstants.PARAM_PATHS, "path/"),
                        Arrays.asList("restore", "path/")},

                {CollectionsUtil.asMap(
                        DotnetConstants.PARAM_RESTORE_PACKAGES, "packages/",
                        DotnetConstants.PARAM_RESTORE_PARALLEL, "false"),
                        Arrays.asList("restore", "--packages", "packages/")},

                {CollectionsUtil.asMap(DotnetConstants.PARAM_RESTORE_PARALLEL, "true"),
                        Arrays.asList("restore", "--disable-parallel")},
        };
    }

    @DataProvider(name = "testPublishArgumentsData")
    public Object[][] testPublishArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DotnetConstants.PARAM_PATHS, "path/"),
                        Arrays.asList("publish", "path/")},

                {CollectionsUtil.asMap(
                        DotnetConstants.PARAM_PUBLISH_FRAMEWORK, "dotcore",
                        DotnetConstants.PARAM_PUBLISH_CONFIG, "Release"),
                        Arrays.asList("publish", "--framework", "dotcore", "--configuration", "Release")},

                {CollectionsUtil.asMap(
                        DotnetConstants.PARAM_PUBLISH_RUNTIME, "active",
                        DotnetConstants.PARAM_PUBLISH_NATIVE, "true"),
                        Arrays.asList("publish", "--runtime", "active", "--native-subdirectory")},
        };
    }

    @DataProvider(name = "testPackArgumentsData")
    public Object[][] testPackArgumentsData() {
        return new Object[][]{
                {CollectionsUtil.asMap(DotnetConstants.PARAM_PATHS, "path/"),
                        Arrays.asList("pack", "path/")},

                {CollectionsUtil.asMap(
                        DotnetConstants.PARAM_PACK_CONFIG, "Release"),
                        Arrays.asList("pack", "--configuration", "Release")},

                {CollectionsUtil.asMap(
                        DotnetConstants.PARAM_PACK_OUTPUT, "output/",
                        DotnetConstants.PARAM_ARGUMENTS, "--quiet"),
                        Arrays.asList("pack", "--output", "output/", "--quiet")},
        };
    }
}
