package jetbrains.buildServer.dotnet.discovery

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.regex.Pattern

class JsonProjectDeserializer(
        private val _readerFactory: ReaderFactory)
    : SolutionDeserializer {

    private val _gson: Gson

    init {
        val builder = GsonBuilder()
        _gson = builder.create()
    }

    override fun accept(path: String): Boolean = PathPattern.matcher(path).find()

    override fun deserialize(path: String, streamFactory: StreamFactory): Solution =
            streamFactory.tryCreate(path)?.let {
                it.use {
                    _readerFactory.create(it).use {
                        val project = _gson.fromJson(it, JsonProjectDto::class.java)
                        val configurations = project.configurations?.keys?.map { Configuration(it) } ?: emptyList()
                        val frameworks = project.frameworks?.keys?.map { Framework(it) } ?: emptyList()
                        val runtimes = project.runtimes?.keys?.map { Runtime(it) } ?: emptyList()
                        Solution(listOf(Project(path, configurations, frameworks, runtimes, emptyList())))
                    }
                }
            } ?: Solution(emptyList())

    private companion object {
        private val PathPattern: Pattern = Pattern.compile("^(.+[^\\w\\d]|)project\\.json$", Pattern.CASE_INSENSITIVE)
    }
}