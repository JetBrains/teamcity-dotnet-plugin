package jetbrains.buildServer.dotnet

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
        }
    }

    @Bean
    open fun cacheManager(): CacheManager {
        return CustomCacheManager { TimeEvictStrategy(Duration.ofMinutes(1)) }
    }
}