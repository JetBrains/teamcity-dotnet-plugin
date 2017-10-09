package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.runner.Converter

interface DotCoverFilterConverter: Converter<String, Sequence<CoverageFilter>> {
}