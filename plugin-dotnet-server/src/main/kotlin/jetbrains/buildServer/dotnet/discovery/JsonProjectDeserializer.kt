

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