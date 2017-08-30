package jetbrains.buildServer.dotcover

interface CoverageFilterProvider {
    val filters: Sequence<CoverageFilter>
    val attributeFilters: Sequence<CoverageFilter>
}