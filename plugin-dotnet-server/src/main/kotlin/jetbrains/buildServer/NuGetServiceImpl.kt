/*
 * Copyright 2000-2020 JetBrains s.r.o.
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

package jetbrains.buildServer

import com.google.gson.JsonParser
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.dotnet.SemanticVersionParser
import org.springframework.cache.annotation.Cacheable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.net.URL

open class NuGetServiceImpl(
        private val _httpDownloader: HttpDownloader,
        private val _versionParser: SemanticVersionParser)
    : NuGetService {

    @Cacheable("getPackagesById")
    override fun getPackagesById(packageId: String, allowPrerelease: Boolean): Sequence<NuGetPackage> {
        return enumeratePackagesById(packageId, allowPrerelease).toList().asSequence()
    }

    private fun enumeratePackagesById(packageId: String, allowPrerelease: Boolean) = sequence {
        LOG.info("Downloading list of packages for $packageId")
        var counter = 0
        val listOfPackagesStream = ByteArrayOutputStream()
        _httpDownloader.download(URL("https://api-v2v3search-0.nuget.org/query?prerelease=$allowPrerelease;q=packageid:$packageId"), listOfPackagesStream)
        val listOfPackagesJson = InputStreamReader(ByteArrayInputStream(listOfPackagesStream.toByteArray()))
        val listOfPackagesObj = JsonParser.parse(listOfPackagesJson).asJsonObject
        val dataArray = listOfPackagesObj.get("data").asJsonArray
        for (nuGetPackage in dataArray) {
            val nuGetPackageObj = nuGetPackage.asJsonObject
            val versionItems = nuGetPackageObj.get("versions").asJsonArray
            for (versionItem in versionItems) {
                val versionItemObj = versionItem.asJsonObject
                val version = versionItemObj.get("version").asString
                val packageVersion = _versionParser.tryParse(version) ?: continue

                val packageInfoUrl = URL(versionItemObj.get("@id").asString)
                val packageInfoStream = ByteArrayOutputStream()
                _httpDownloader.download(packageInfoUrl, packageInfoStream)
                val packageInfoJson = InputStreamReader(ByteArrayInputStream(packageInfoStream.toByteArray()))
                val packageInfoObj = JsonParser.parse(packageInfoJson).asJsonObject
                val downloadUrl = URL(packageInfoObj.get("packageContent").asString)
                val isListed = packageInfoObj.get("listed").asBoolean

                yield(NuGetPackage(packageId, packageVersion, downloadUrl, isListed))
                counter++
            }
        }

        LOG.info("Downloaded list of $counter packages for $packageId")
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(NuGetServiceImpl::class.java.name)
        private val JsonParser = JsonParser()
    }
}