package jetbrains.buildServer.agent.runner

interface Converter<TSource, TDestination> {
    fun convert(source: TSource): TDestination
}