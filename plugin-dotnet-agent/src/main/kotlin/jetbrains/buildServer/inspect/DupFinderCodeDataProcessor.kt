package jetbrains.buildServer.inspect

import jetbrains.buildServer.agent.DataProcessor
import jetbrains.buildServer.agent.DataProcessorContext
import jetbrains.buildServer.agent.FileSystemService
import jetbrains.buildServer.agent.duplicates.DuplicatesReporter
import jetbrains.buildServer.agent.inspections.*
import jetbrains.buildServer.duplicator.DuplicateInfo
import java.util.zip.CRC32

class DupFinderCodeDataProcessor(
        private val _fileSystem: FileSystemService,
        private val _xmlReader: XmlReader,
        private val _reporter: DuplicatesReporter)
    : DataProcessor {
    override fun getType() = DupFinderConstants.DATA_PROCESSOR_TYPE

    override fun processData(context: DataProcessorContext) {
        _reporter.startDuplicates()

        try {
            _fileSystem.read(context.file) {
                var cost: Int? = null
                var fragment: Fragment? = null
                var fragments = mutableListOf<Fragment>()
                var hashes = mutableSetOf<Int>()
                var duplicates = mutableListOf<DuplicateInfo>()

                for (e in _xmlReader.read(it)) {
                    when (e.name.toLowerCase()) {
                        "duplicate" -> {
                            tryCreateDuplicate(cost, fragments, hashes)?.let {
                                duplicates.add(it)
                                publish(duplicates, false)
                            }

                            fragments = mutableListOf()
                            cost = e["Cost"]?.toIntOrNull()
                        }

                        "fragment" -> {
                            fragment = Fragment()
                            fragments.add(fragment)
                        }

                        "filename" -> {
                            fragment?.let {
                                it.fileName = e.value
                            }
                        }

                        "linerange" -> {
                            fragment?.let {
                                it.lineRangeStart = e["Start"]?.toIntOrNull() ?: -1
                                it.lineRangeEnd = e["End"]?.toIntOrNull() ?: -1
                            }
                        }

                        "text" -> {
                            fragment?.let {
                                it.text = e.value
                            }
                        }
                    }
                }

                tryCreateDuplicate(cost, fragments, hashes)?.let { duplicates.add(it) }
                publish(duplicates, true)
            }
        }
        finally {
            _reporter.finishDuplicates()
        }
    }

    private fun tryCreateDuplicate(cost: Int?, fragments: List<Fragment>, hashes: MutableSet<Int>): DuplicateInfo?
    {
        if (cost == null) {
            return null
        }

        val curFragments = fragments.filter { !it.isEmpty }.toList()

        if (!curFragments.any()) {
            return null
        }

        return DuplicateInfo(
                calculateDuplicateHash(curFragments, hashes),
                cost,
                curFragments
                        .map {
                            DuplicateInfo.Fragment(
                                    calculateFragmentHash(it, hashes),
                                    it.fileName?.replace('\\', '/')?: "",
                                    it.lineRangeStart,
                                    DuplicateInfo.LineOffset(it.lineRangeStart, it.lineRangeEnd)
                            )
                        }.toTypedArray())
    }

    private fun calculateFragmentHash(fragment: Fragment, hashes: MutableSet<Int>): Int {
        val crc32 = CRC32()
        fragment.updateCRC32(crc32)
        return ensureHashIsUniqueWithinOneBuild(crc32.value.toInt(), hashes)
    }

    private fun calculateDuplicateHash(fragments: List<Fragment>, hashes: MutableSet<Int>): Int {
        val crc32 = CRC32()
        for (fragment in fragments) {
            fragment.updateCRC32(crc32)
        }

        return ensureHashIsUniqueWithinOneBuild(crc32.value.toInt(), hashes)
    }

    private fun ensureHashIsUniqueWithinOneBuild(hash: Int, hashes: MutableSet<Int>): Int {
        var initialHash = hash
        while (hashes.contains(initialHash)) {
            initialHash++
        }

        hashes.add(initialHash)
        return initialHash
    }

    private fun publish(duplicates: MutableCollection<DuplicateInfo>, force: Boolean) {
        if (duplicates.size > 90 || (duplicates.size > 0 && force)) {
            _reporter.addDuplicates(duplicates)
            duplicates.clear()
        }
    }

    private class Fragment() {
        public var text: String? = null
        public var fileName: String? = null
        public var lineRangeStart: Int = -1
        public var lineRangeEnd: Int = -1

        val isEmpty get() = fileName == null

        fun updateCRC32(crc32: CRC32) {
            val curText = text
            if(curText != null) {
                crc32.update(curText.toByteArray())
            }
            else {
                crc32.update((fileName?: "").toByteArray())
                crc32.update(lineRangeStart)
                crc32.update(lineRangeEnd)
            }
        }
    }
}