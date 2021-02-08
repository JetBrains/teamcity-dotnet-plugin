package jetbrains.buildServer

import java.io.Reader

interface JsonParser {
    fun <T>tryParse(json: Reader, classOfT: Class<T>): T?
}