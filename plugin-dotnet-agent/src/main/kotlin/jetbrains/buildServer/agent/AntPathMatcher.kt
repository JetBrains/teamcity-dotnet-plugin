package jetbrains.buildServer.agent

import jetbrains.buildServer.util.pathMatcher.AntPatternFileCollector
import java.io.File

class AntPathMatcher : PathMatcher {
    override fun match(path: File, includeRules: List<String>): List<File> =
            AntPatternFileCollector.scanDir(path, includeRules.toTypedArray(), emptyArray(),
                    AntPatternFileCollector.ScanOption.PRIORITIZE_EXCLUDES, AntPatternFileCollector.ScanOption.ALLOW_EXTERNAL_SCAN)

}