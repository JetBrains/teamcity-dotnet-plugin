package jetbrains.buildServer

import java.io.Reader

class JsonParserImpl : JsonParser {
    override fun <T> tryParse(reader: Reader, classOfT: Class<T>): T? =
        Gson.fromJson<T>(reader, classOfT)

    companion object {
        private val Gson = com.google.gson.Gson()
    }
}