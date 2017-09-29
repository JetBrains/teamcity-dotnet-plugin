package jetbrains.buildServer.dotnet

import jetbrains.buildServer.tools.*
import jetbrains.buildServer.tools.ToolType
import jetbrains.buildServer.util.TimeService
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.support.AbstractBeanFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.cache.ehcache.EhCacheCacheManager
import org.springframework.cache.guava.GuavaCacheManager
import org.springframework.cache.jcache.JCacheCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
open class BeanConfig(
        beanFactory: AbstractBeanFactory,
        parameters: DotnetParametersProvider,
        pluginDescriptor: PluginDescriptor,
        timeService: TimeService,
        packageVersionParser: NuGetPackageVersionParser,
        httpDownloader: HttpDownloader,
        nuGetService: NuGetService,
        _fileSystemService: FileSystemService) {

    init {
        if (parameters.experimentalMode) {
            beanFactory.registerSingleton(
                    DotnetToolProviderAdapter::class.java.name,
                    DotnetToolProviderAdapter(pluginDescriptor, timeService, packageVersionParser, httpDownloader, nuGetService, _fileSystemService))
        }
    }

    @Bean
    public open fun cacheManager(): CacheManager {
        return CustomCacheManager { TimeEvictStrategy(Duration.ofMinutes(1)) }
    }
}