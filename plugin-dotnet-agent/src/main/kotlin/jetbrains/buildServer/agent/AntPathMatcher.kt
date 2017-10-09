package jetbrains.buildServer.agent

import jetbrains.buildServer.util.pathMatcher.AntPatternFileCollector
import java.io.File

class AntPathMatcher : PathMatcher {
    override fun match(path: File, includeRules: Sequence<String>, excludeRules: Sequence<String>): Sequence<File> {
        return AntPatternFileCollector.scanDir(path, includeRules.toList().toTypedArray(), excludeRules.toList().toTypedArray(), ScanOptions).asSequence()
    }

    companion object {
        private val ScanOptions = arrayOf(AntPatternFileCollector.ScanOption.PRIORITIZE_EXCLUDES, AntPatternFileCollector.ScanOption.ALLOW_EXTERNAL_SCAN)
    }
}