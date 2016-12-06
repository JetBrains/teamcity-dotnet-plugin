/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet.test

import jetbrains.buildServer.dotnet.DotnetModelParser
import jetbrains.buildServer.util.browser.Element
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream


/**
 * @author Dmitry.Tretyakov
 *         Date: 06.12.2016
 *         Time: 18:17
 */
class DotnetModelParserTest {
    @Test
    fun getCsProjectModel() {
        val m = Mockery()
        val element = m.mock(Element::class.java)
        val parser = DotnetModelParser()
        val csproj = File("src/test/resources/project.csproj")

        m.checking(object : Expectations() {
            init {
                one(element).isContentAvailable
                will(returnValue(true))

                one(element).inputStream
                will(returnValue(BufferedInputStream(FileInputStream(csproj))))
            }
        })

        val project = parser.getCsProjectModel(element)

        Assert.assertNotNull(project)
        Assert.assertEquals(project!!.ToolsVersion, "15.0")

        Assert.assertNotNull(project.PropertyGroups)
        project.PropertyGroups?.let {
            Assert.assertEquals(it.size, 1)
            Assert.assertEquals(it[0].TargetFramework, "netcoreapp1.0")
            Assert.assertNull(it[0].TargetFrameworks)
        }

        Assert.assertNotNull(project.ItemGroups)
        project.ItemGroups?.let {
            Assert.assertEquals(it.size, 2)
            Assert.assertNotNull(it[1].PackageReferences)

            it[1].PackageReferences?.let {
                Assert.assertEquals(it.size, 5)
                Assert.assertEquals(it[4].Include, "xunit.runner.visualstudio")
            }
        }
    }
}