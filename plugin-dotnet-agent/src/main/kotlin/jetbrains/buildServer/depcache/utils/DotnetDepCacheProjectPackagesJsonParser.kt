package jetbrains.buildServer.depcache.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jetbrains.buildServer.depcache.DotnetDepCacheListPackagesResult

object DotnetDepCacheProjectPackagesJsonParser {

    private val GSON = Gson()
    private val TYPE = object : TypeToken<DotnetDepCacheListPackagesResult>() {}.type

    fun fromCommandLineOutput(commandLineOutput: String): Result<DotnetDepCacheListPackagesResult> {
        return runCatching { GSON.fromJson(commandLineOutput, TYPE) }
    }
}