/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.dotnet.models.Project
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.browser.Element
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

/**
 * Provides serialization capabilities.
 */
class DotnetModelParser {

    private val myGson: Gson
    private val myXmlMapper: ObjectMapper
    private val ProjectPathPattern: Regex = Regex(""""([^\"]+\${DotnetConstants.PROJECT_CSPROJ})"""")

    init {
        val builder = GsonBuilder()
        myGson = builder.create()
        myXmlMapper = XmlMapper()
        myXmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        myXmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        myXmlMapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
    }

    fun getProjectModel(element: Element?): Project? {
        if (element == null || !element.isContentAvailable) {
            return null
        }

        try {
            val inputStream = getInputStreamReader(element.inputStream)
            BufferedReader(inputStream).use {
                return myGson.fromJson(it, Project::class.java)
            }
        } catch (e: Exception) {
            val message = "Failed to retrieve file for given path ${element.fullName}: $e"
            Loggers.SERVER.infoAndDebugDetails(message, e)
        }

        return null
    }

    fun getCsProjectModel(element: Element?): CsProject? {
        if (element == null || !element.isContentAvailable) {
            return null
        }

        try {
            val inputStream = getInputStreamReader(element.inputStream)
            BufferedReader(inputStream).use {
                val project = myXmlMapper.readValue(it, CsProject::class.java)
                project.path = element.fullName
                return project
            }
        } catch (e: Exception) {
            val message = "Failed to retrieve file for given path ${element.fullName}: $e"
            Loggers.SERVER.infoAndDebugDetails(message, e)
        }

        return null
    }

    fun getCsProjectModels(element: Element?): List<CsProject>? {
        if (element == null || !element.isContentAvailable) {
            return null
        }

        val projectPaths = arrayListOf<String>()
        try {
            val inputStream = getInputStreamReader(element.inputStream)
            BufferedReader(inputStream).use {
                inputStream.readLines().forEach {
                    ProjectPathPattern.find(it)?.let {
                        projectPaths.add(it.groupValues[1].replace('\\', '/'))
                    }
                }
            }
        } catch (e: Exception) {
            val message = "Failed to retrieve file for given path ${element.fullName}: $e"
            Loggers.SERVER.infoAndDebugDetails(message, e)
        }

        return projectPaths.map { getElement(element, it) }
                .filterNotNull()
                .map { getCsProjectModel(it) }
                .filterNotNull()
    }

    private fun getElement(element: Element, it: String): Element? {
        val fullName = element.fullName
        val parent = fullName.substring(0, fullName.length - element.name.length)
        return element.browser.getElement("$parent$it")
    }

    private fun getInputStreamReader(inputStream: InputStream): Reader {
        inputStream.mark(3)
        val byte1 = inputStream.read()
        val byte2 = inputStream.read()
        if (byte1 == 0xFF && byte2 == 0xFE) {
            return InputStreamReader(inputStream, "UTF-16LE")
        } else if (byte1 == 0xFF && byte2 == 0xFF) {
            return InputStreamReader(inputStream, "UTF-16BE")
        } else {
            val byte3 = inputStream.read()
            if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF) {
                return InputStreamReader(inputStream, "UTF-8")
            } else {
                inputStream.reset()
                return InputStreamReader(inputStream)
            }
        }
    }
}
