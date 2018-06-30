package jetbrains.buildServer.dotnet

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
        pluginDescriptor: PluginDescriptor,
        packageVersionParser: SemanticVersionParser,
        httpDownloader: HttpDownloader,
        nuGetService: NuGetService,
        _fileSystemService: FileSystemService) {

    init {
        if (parameters.experimentalMode) {
            beanFactory.registerSingleton(
                    DotnetToolProviderAdapter::class.java.name,
                    DotnetToolProviderAdapter(pluginDescriptor, packageVersionParser, httpDownloader, nuGetService, _fileSystemService))
        } else {
            val toolIds = installedToolsState.all.filter { it.toolType == DotnetConstants.PACKAGE_TYPE }.map { it.toolId }.toList()
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