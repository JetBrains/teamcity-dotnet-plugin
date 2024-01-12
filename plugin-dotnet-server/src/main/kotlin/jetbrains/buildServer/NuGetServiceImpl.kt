

package jetbrains.buildServer

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import org.springframework.cache.annotation.Cacheable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.URL

open class NuGetServiceImpl(
        private val _httpDownloader: HttpDownloader)
    : NuGetService {

    @Cacheable("getPackagesById", sync = true)
    override fun getPackagesById(packageId: String) = sequence {
        val searchQueryServiceUrl = enumerateServices("SearchQueryService").firstOrNull()
        if (searchQueryServiceUrl == null) {
            LOG.warn("Cannot find Nuget Search Query Service")
        }

        val packageBaseUrl = enumerateServices("PackageBaseAddress/3.0.0").firstOrNull()
        if (searchQueryServiceUrl == null) {
            LOG.warn("Cannot find Nuget PackageBaseAddress/3.0.0")
        }

        ByteArrayOutputStream().use {
            stream ->
            var counter = 0
            _httpDownloader.download(URL(searchQueryServiceUrl, "query?q=packageid:$packageId&prerelease=true"), stream)
            val json = InputStreamReader(ByteArrayInputStream(stream.toByteArray()))
            val rootObj = JsonParser.parseReader(json).asJsonObject
            val dataArray = rootObj.get("data").asJsonArray
            for (nuGetPackage in dataArray) {
                val nuGetPackageObj = nuGetPackage.asJsonObject
                val versionItems = nuGetPackageObj.get("versions").asJsonArray
                for (versionItem in versionItems) {
                    val versionItemObj = versionItem.asJsonObject
                    val packageVersion = versionItemObj.get("version").asString
                    val packageUrl = URL(packageBaseUrl, "$packageId/$packageVersion/$packageId.$packageVersion.nupkg".lowercase())
                    yield(NuGetPackage(packageId, packageVersion, packageUrl))
                    counter++
                }

                LOG.debug("Downloaded list of $counter packages for $packageId")
            }
        }
    }

    private fun enumerateServices(serviceName: String) = sequence {
        ByteArrayOutputStream().use {
            stream ->
            _httpDownloader.download(NugetFeed, stream)
            val json = InputStreamReader(ByteArrayInputStream(stream.toByteArray()))
            val rootObj = JsonParser.parseReader(json).asJsonObject
            val resourcesObj = rootObj.get("resources").asJsonArray
            for (resource in resourcesObj) {
                val resourceObj = resource.asJsonObject
                if (resourceObj.get("@type").asString?.startsWith(serviceName, false) == true) {
                    val serviceUrl = URL(resourceObj.get("@id").asString)
                    LOG.debug("Found Nuget $serviceName: $serviceUrl")
                    yield(serviceUrl)
                }
            }
        }
    }

    companion object {
        private val NugetFeed = URL("https://api.nuget.org/v3/index.json")
        private val LOG: Logger = Logger.getInstance(NuGetServiceImpl::class.java.name)
    }
}