package jetbrains.buildServer.nunit.testReordering

import java.io.File

class RunnerConfigVarTestInfoParser {
    fun parse(text: String): List<TestInfo> {
        val tests = mutableListOf<TestInfo>()

        for (splitItem in text.split(LINE_SEPARATOR)) {
            if (splitItem.isBlank()) {
                continue
            }

            val item = splitItem.trim { it <= ' ' }
            val separatorIndex = item.lastIndexOf(ASSEMBLY_AND_CLASS_SEPARATOR)
            if (separatorIndex > 0 && separatorIndex < item.length - 1) {
                val assemblyName = item.substring(0, separatorIndex).trim { it <= ' ' }
                val className = item.substring(separatorIndex + 1).trim { it <= ' ' }
                if (assemblyName.isNotEmpty() && className.isNotEmpty()) {
                    tests.add(TestInfo(File(assemblyName), className, null))
                    continue
                }
            }

            tests.add(TestInfo(item))
        }

        return tests
    }

    companion object {
        private const val LINE_SEPARATOR = "\n"
        private const val ASSEMBLY_AND_CLASS_SEPARATOR = ":"
    }
}