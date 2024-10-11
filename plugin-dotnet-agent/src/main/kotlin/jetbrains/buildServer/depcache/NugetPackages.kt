package jetbrains.buildServer.depcache

import com.google.gson.Gson
import jetbrains.buildServer.agent.cache.depcache.invalidation.Serializable
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Objects

data class NugetPackages(private val cacheRootPackages: Map<String, Set<String>>) : Serializable {

    override fun serialize(): ByteArray {
        return GSON.toJson(this).toByteArray(JSON_CHARSET)
    }

    companion object {
        private val JSON_CHARSET: Charset = StandardCharsets.UTF_8
        private val GSON = Gson()

        fun deserialize(bytes: ByteArray): NugetPackages {
            return GSON.fromJson(String(bytes, JSON_CHARSET), NugetPackages::class.java)
        }
    }
}