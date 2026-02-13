package jetbrains.buildServer.dotcover

import jetbrains.buildServer.agent.ArgumentsService
import jetbrains.buildServer.agent.runner.LoggerService
import jetbrains.buildServer.dotcover.command.DotCoverCommandType
import jetbrains.buildServer.dotnet.commands.msbuild.MSBuildParameterNormalizer.normalizeAndQuoteValue
import java.io.OutputStream
import java.io.PrintWriter

class DotCoverResponseFileSerializerImpl(
    private val _argumentsService: ArgumentsService,
    private val _coverageFilterProvider: CoverageFilterProvider,
    private val _loggerService: LoggerService
) : DotCoverResponseFileSerializer {
    override fun serialize(obj: DotCoverProject, outputStream: OutputStream) {
        val writer = PrintWriter(outputStream)
        when (obj.dotCoverCommandType) {
            DotCoverCommandType.Cover -> serializeForCover(requireNotNull(obj.coverCommandData), writer)
            DotCoverCommandType.Merge -> serializeForMerge(requireNotNull(obj.mergeCommandData), writer)
            DotCoverCommandType.Report -> serializeForReport(requireNotNull(obj.reportCommandData), writer)
        }
        writer.flush()
    }

    private fun serializeForCover(commandData: DotCoverProject.CoverCommandData, writer: PrintWriter) {
        val workingDirectory = commandData.commandLineToCover.workingDirectory
        with (writer) {
            println("--target-executable")
            println(safe(commandData.commandLineToCover.executableFile.path))
            println("--target-working-directory")
            println(safe(workingDirectory.path))
            println("--snapshot-output")
            println(safe(commandData.snapshotFile.path))

            // --exclude-assemblies
            val excludeAssemblies = mutableListOf<String>()
            for (filter in _coverageFilterProvider.filters) {
                when (filter.type) {
                    CoverageFilter.CoverageFilterType.Include -> {
                        if (filter.moduleMask != CoverageFilter.Any
                            || filter.classMask != CoverageFilter.Any
                            || filter.functionMask != CoverageFilter.Any) {
                            _loggerService.writeWarning(
                                "Coverage filter '$filter' cannot be applied: " +
                                        "include filters are not supported in dotCover 2025.2+"
                            )
                        }
                    }

                    CoverageFilter.CoverageFilterType.Exclude -> {
                        val hasClassOrFunction = filter.classMask != CoverageFilter.Any
                                || filter.functionMask != CoverageFilter.Any

                        if (filter.moduleMask == CoverageFilter.Any && hasClassOrFunction) {
                            _loggerService.writeWarning(
                                "Coverage filter '$filter' cannot be applied: " +
                                        "class/function-level filters are not supported in dotCover 2025.2+"
                            )
                            continue
                        }

                        if (hasClassOrFunction) {
                            _loggerService.writeWarning(
                                "Coverage filter '$filter' is partially applied: " +
                                        "only the assembly mask '${filter.moduleMask}' will be used; " +
                                        "class/function-level filters are not supported in dotCover 2025.2+"
                            )
                        }

                        excludeAssemblies.add(filter.moduleMask)
                    }

                    else -> { }
                }
            }

            if (excludeAssemblies.isNotEmpty()) {
                println("--exclude-assemblies")
                println(excludeAssemblies.joinToString(","))
            }

            // --exclude-attributes
            val excludeAttributes = _coverageFilterProvider.attributeFilters
                .map { it.classMask }
                .filter { it != CoverageFilter.Any }
                .toList()
            if (excludeAttributes.isNotEmpty()) {
                println("--exclude-attributes")
                println(excludeAttributes.joinToString(","))
            }

            if (commandData.commandLineToCover.arguments.isNotEmpty()) {
                println("--") // alias for --target-arguments
                println(_argumentsService.combine(commandData.commandLineToCover.arguments.map { it.value }.asSequence()))
            }
        }
    }

    private fun serializeForMerge(commandData: DotCoverProject.MergeCommandData, writer: PrintWriter) {
        with(writer) {
            println("--snapshot-source")
            println(safe(commandData.sourceFiles.map { it.path }.joinToString(",")))
            println("--snapshot-output")
            println(safe(commandData.outputFile.path))
        }
    }

    private fun serializeForReport(commandData: DotCoverProject.ReportCommandData, writer: PrintWriter) {
        with(writer) {
            println("--snapshot-source")
            println(safe(commandData.sourceFile.path))
            println("--xml-report-output")
            println(safe(commandData.outputFile.path))
        }
    }

    private fun safe(value: String): String {
        return normalizeAndQuoteValue(value, fullEscaping = false, quoteOnOneTrailingBackslash = false)
    }
}