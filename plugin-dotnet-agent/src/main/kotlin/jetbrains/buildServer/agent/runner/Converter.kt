package jetbrains.buildServer.agent.runner

interface Converter<in TSource, out TDestination> {
    fun convert(source: TSource): TDestination
}