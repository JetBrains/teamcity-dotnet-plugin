/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * See LICENSE in the project root for license information.
 */

package jetbrains.buildServer.dotnet

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import jetbrains.buildServer.dotnet.models.CsProject
import jetbrains.buildServer.dotnet.models.Project
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.browser.Element
import java.io.*
import javax.xml.bind.JAXBContext

/**
 * Provides serialization capabilities.
 */
class DotnetModelParser {

    private val myGson: Gson

    init {
        val builder = GsonBuilder()
        myGson = builder.create()
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
            val jaxbContext = JAXBContext.newInstance(CsProject::class.java)
            val jaxbUnmarshaller = jaxbContext.createUnmarshaller()
            val inputStream = getInputStreamReader(element.inputStream)
            BufferedReader(inputStream).use {
                return jaxbUnmarshaller.unmarshal(it) as CsProject
            }
        } catch (e: Exception) {
            val message = "Failed to retrieve file for given path ${element.fullName}: $e"
            Loggers.SERVER.infoAndDebugDetails(message, e)
        }

        return null
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
