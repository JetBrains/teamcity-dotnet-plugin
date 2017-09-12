package jetbrains.buildServer.agent

import jetbrains.buildServer.util.pathMatcher.AntPatternFileCollector
import java.io.File

class AntPathMatcher : PathMatcher {
    override fun match(path: File, includeRules: Sequence<String>, excludeRules: Sequence<String>): Sequence<File> {
        return AntPatternFileCollector.scanDir(path, includeRules.toList().toTypedArray(), excludeRules.toList().toTypedArray(), _ourScanOptions).asSequence()
    }

    companion object {
        private val _ourScanOptions = arrayOf<AntPatternFileCollector.ScanOption>(AntPatternFileCollector.ScanOption.PRIORITIZE_EXCLUDES, AntPatternFileCollector.ScanOption.ALLOW_EXTERNAL_SCAN)
    }
}