package jetbrains.buildServer.runners

interface Converter<TSource, TDestination> {
    fun convert(source: TSource): TDestination
}