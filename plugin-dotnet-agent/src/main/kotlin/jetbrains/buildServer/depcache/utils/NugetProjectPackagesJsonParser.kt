package jetbrains.buildServer.depcache.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jetbrains.buildServer.depcache.DotnetListPackagesResult

object NugetProjectPackagesJsonParser {

    private val GSON = Gson()
    private val TYPE = object : TypeToken<DotnetListPackagesResult>() {}.type

    fun fromCommandLineOutput(commandLineOutput: String): Result<DotnetListPackagesResult> {
        return runCatching { GSON.fromJson(commandLineOutput, TYPE) }
    }
}