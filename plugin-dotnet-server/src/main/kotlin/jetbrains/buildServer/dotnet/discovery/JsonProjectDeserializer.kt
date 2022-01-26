/*
 * Copyright 2000-2022 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.dotnet.discovery

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import jetbrains.buildServer.JsonParser
import java.util.regex.Pattern

class JsonProjectDeserializer(
        private val _jsonParser: JsonParser,
        private val _readerFactory: ReaderFactory)
    : SolutionDeserializer {

    override fun isAccepted(path: String): Boolean = PathPattern.matcher(path).find()

    override fun deserialize(path: String, streamFactory: StreamFactory): Solution =
            streamFactory.tryCreate(path)?.let {
                it.use {
                    _readerFactory.create(it).use {
                        _jsonParser.tryParse<JsonProjectDto>(it, JsonProjectDto::class.java)?.let {
                            project ->
                            val configurations = project.configurations?.keys?.map { Configuration(it) } ?: emptyList()
                            val frameworks = project.frameworks?.keys?.map { Framework(it) } ?: emptyList()
                            val runtimes = project.runtimes?.keys?.map { Runtime(it) } ?: emptyList()
                            Solution(listOf(Project(path, configurations, frameworks, runtimes, emptyList())))
                        }
                    }
                }
            } ?: Solution(emptyList())

    private companion object {
        private val PathPattern: Pattern = Pattern.compile("^(.+[^\\w\\d]|)project\\.json$", Pattern.CASE_INSENSITIVE)
    }
}