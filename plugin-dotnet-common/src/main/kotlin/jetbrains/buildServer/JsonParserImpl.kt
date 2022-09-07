package jetbrains.buildServer

import java.io.Reader

class JsonParserImpl : JsonParser {
    override fun <T> tryParse(json: Reader, classOfT: Class<T>): T? =
        Gson.fromJson<T>(json, classOfT)

    companion object {
        private val Gson = com.google.gson.Gson()
    }
}