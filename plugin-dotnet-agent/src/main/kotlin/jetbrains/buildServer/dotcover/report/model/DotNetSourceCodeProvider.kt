package jetbrains.buildServer.dotcover.report.model

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.text.CharArrayCharSequence
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.coverage.utils.Distances
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.StringUtil
import java.io.File
import java.io.IOException
import java.text.MessageFormat
import java.util.Collections
import java.util.LinkedList
import java.util.Locale
import java.util.TreeSet

class DotNetSourceCodeProvider(private val _checkoutDir: File) {

    private val _files: MutableMap<Int, File> = HashMap()
    private var _encoding: String? = null

    fun addFile(id: Int, path: File) {
        _files[id] = path
    }

    val files: Set<File>
        get() = HashSet(_files.values)

    private fun mapFiles(params: DotnetCoverageParameters) {
        _encoding = params.getConfigurationParameter(FILES_ENCODING_KEY)

        val mapping: String? = params.getConfigurationParameter(SOURCE_MAPPING_KEY)
        if (mapping == null || StringUtil.isEmptyOrSpaces(mapping)) {
            LOG.debug("No mapping specified.")
            return
        }

        var parse = mapping.split("=>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parse.size == 1 && mapping.endsWith("=>")) parse = arrayOf(parse[0], "")
        if (parse.size != 2) {
            val message = "Failed to parse source mapping: \r\n$mapping"
            LOG.warn(message)
            params.getBuildLogger().warning(message)
            return
        }

        val file = FileUtil.resolvePath(_checkoutDir, parse[0].trim { it <= ' ' })
        val from = makeComparable(file)
        val to = parse[1].trim { it <= ' ' }

        LOG.debug("Parsed mapping rule: $from=>$to")
        params.getBuildLogger()
            .message("Use sources mapping to map reported by dotCover paths to the build sources:\r\n$mapping")

        var count = 0
        for (e: MutableMap.MutableEntry<Int, File> in _files.entries) {
            val s = makeComparable(e.value)
            val result: String
            if (s == from) {
                result = to
            } else if (s.startsWith("$from/")) {
                result = to + s.substring(from.length)
            } else {
                continue
            }
            e.setValue(FileUtil.resolvePath(_checkoutDir, result))
            count++
        }
        params.getBuildLogger().message("Mapped $count source file(s).")
    }

    private fun makeComparable(file: File): String {
        val canonicalFile = FileUtil.getCanonicalFile(file)
        val path = canonicalFile.path.replace("[\\\\/]+".toRegex(), "/").replace("/+$".toRegex(), "")

        return if (SystemInfo.isFileSystemCaseSensitive) path else path.lowercase(Locale.getDefault())
    }

    private fun validateFoundFiles(params: DotnetCoverageParameters) {
        val checkoutDir = makeComparable(_checkoutDir)
        var fails = 0
        val totalFiles = _files.size
        val errors = FilteringList()
        val it: MutableIterator<Map.Entry<Int, File>> = _files.entries.iterator()
        while (it.hasNext()) {
            val entry = it.next()
            val path = entry.value
            if (!makeComparable(path).startsWith(checkoutDir)) {
                fails++
                errors.addValue(path.path)
                it.remove()
            }
        }

        if (_files.isEmpty()) {
            val message = MessageFormat.format(
                "No source files were found under the build checkout " +
                        "directory {0}. No source files will be included in dotCover " +
                        "report as source code of classes.", _checkoutDir.path)
            LOG.warn(message)
            params.getBuildLogger().warning(message)

            if (fails > 0) {
                dumpNotFoundFiles(params, errors)
            }
        } else if (fails > 0) {
            params.getBuildLogger().warning(
                MessageFormat.format(
                    ("{0} of {1} source file{2} were not found under the build checkout " +
                            "directory {3}. Those files will not be included in dotCover " +
                            "report as source code of classes."),
                    fails,
                    totalFiles,
                    if (fails > 1) "s" else "",
                    _checkoutDir.path))
            dumpNotFoundFiles(params, errors)
        }
    }

    private fun dumpNotFoundFiles(params: DotnetCoverageParameters,
                                  errors: FilteringList) {
        params.getBuildLogger().warning(
            MessageFormat.format("For example: \r\n    {0}",
                StringUtil.join(errors.entries, "\r\n    ")))
    }


    private class FilteringList {
        private val myList: MutableList<String> = LinkedList()
        val entries: Collection<String>
            get() = Collections.unmodifiableCollection(myList)

        fun addValue(value: String) {
            myList.add(value)
            while (myList.size > 5) {
                var kill: String? = null
                var min = Int.MAX_VALUE
                for (a in myList) {
                    for (b in myList) {
                        if (a == b) continue
                        val dis = Distances.levenshteinDistance(a, b)
                        if (dis < min) {
                            min = dis
                            kill = a
                        }
                    }
                }
                myList.remove(kill)
            }
        }
    }

    fun preprocessFoundFiles(params: DotnetCoverageParameters,
                             referredFiles: Set<Int>) {
        if (LOG.isDebugEnabled) {
            LOG.debug("dotCover reported the following source files to include into report: " + TreeSet(_files.values))
        }

        val cleanFiles = "true".equals(params.getConfigurationParameter(NO_SOURCE_FILES_KEY), ignoreCase = true)
        if (cleanFiles) {
            _files.clear()
            LOG.warn("Report will not contain sources. $NO_SOURCE_FILES_KEY configuration parameter was specified.")
            params.getBuildLogger()
                .warning("Report will not contain sources. $NO_SOURCE_FILES_KEY configuration parameter was specified.")
            return
        }

        //Remove all files that were not referenced from sources
        _files.keys.retainAll(referredFiles)
        try {
            mapFiles(params)
            validateFoundFiles(params)
        } catch (t: Throwable) {
            val msg = ("Error processing dotCover reported source files path. " +
                    "No source code will be included into report. " + t.message)
            LOG.warn(msg, t)
            params.getBuildLogger().warning(msg)
            _files.clear()
        }

        if (LOG.isDebugEnabled) {
            LOG.debug("Found the following source files to include into dotCover report: " + TreeSet(_files.values))
        }
    }

    fun getFileContentLines(id: Int): Collection<CharSequence>? {
        val code = loadFile(id) ?: return null
        return jetbrains.coverage.report.impl.StringUtil.getLines(code)
    }

    fun getCaption(id: Int): String? {
        val file = _files[id] ?: return null
        return FileUtil.getRelativePath(_checkoutDir, file)
    }

    private fun loadFile(id: Int): CharSequence? {
        val file = _files[id] ?: return null
        return try {
            CharArrayCharSequence(*FileUtil.loadFileText(file, _encoding))
        } catch (e: IOException) {
            "Failed to load file: $file"
        }
    }

    companion object {
        private val LOG = Logger.getInstance(DotNetSourceCodeProvider::class.java.name)
        private val NO_SOURCE_FILES_KEY = "dotNetCoverage.dotCover.report.excludeSources"
        private val SOURCE_MAPPING_KEY = "dotNetCoverage.dotCover.source.mapping"
        private val FILES_ENCODING_KEY = "dotNetCoverage.dotCover.source.encoding"
    }
}
