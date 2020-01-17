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

import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.dotnet.DotnetParametersProvider
import jetbrains.buildServer.dotnet.DotnetToolProviderAdapter
import jetbrains.buildServer.dotnet.SemanticVersionParser
import jetbrains.buildServer.tools.installed.InstalledToolsState
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.beans.factory.support.AbstractBeanFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@EnableCaching
open class BeanConfig(
        installedToolsState: InstalledToolsState,
        beanFactory: AbstractBeanFactory,
        parameters: DotnetParametersProvider,
        toolService: ToolService,
        pluginDescriptor: PluginDescriptor,
        packageVersionParser: SemanticVersionParser,
        fileSystemService: FileSystemService) {

    init {
        if (parameters.experimentalMode) {
            beanFactory.registerSingleton(
                    DotnetToolProviderAdapter::class.java.name,
                    DotnetToolProviderAdapter(toolService, pluginDescriptor, packageVersionParser, fileSystemService))
        } else {
            val toolIds = installedToolsState.all.filter { it.toolType == DotnetConstants.INTEGRATION_PACKAGE_TYPE }.map { it.toolId }.toList()
            for (toolId in toolIds) {
                installedToolsState.remove(toolId)
            }
        }
    }

    @Bean
    open fun cacheManager(): CacheManager {
        return CustomCacheManager { TimeEvictStrategy(Duration.ofMinutes(1)) }
    }
}