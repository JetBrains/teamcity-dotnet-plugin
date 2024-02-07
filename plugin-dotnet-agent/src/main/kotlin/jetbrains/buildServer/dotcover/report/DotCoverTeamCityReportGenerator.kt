package jetbrains.buildServer.dotcover.report

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotcover.report.model.AssemblyInfo
import jetbrains.buildServer.dotcover.report.model.ClassHolder
import jetbrains.buildServer.dotcover.report.model.DotCoverClass
import jetbrains.buildServer.dotcover.report.model.DotCoverData
import jetbrains.buildServer.dotcover.report.model.NamespaceInfo
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import jetbrains.buildServer.dotnet.coverage.utils.XmlXppAbstractParser
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.WatchDog
import jetbrains.coverage.report.CoverageData
import jetbrains.coverage.report.CoverageStatistics
import jetbrains.coverage.report.Entry
import jetbrains.coverage.report.ReportBuilderFactory
import jetbrains.coverage.report.StatisticsCalculator
import jetbrains.coverage.report.html.HTMLReportBuilder
import java.io.File
import java.io.IOException
import java.util.Arrays
import java.util.concurrent.atomic.AtomicReference

class DotCoverTeamCityReportGenerator {

    @Throws(IOException::class)
    fun parseStatementCoverage(reportFile: File): Entry? {
        if (!reportFile.isFile || reportFile.length() < 10) return null

        val entry: AtomicReference<Entry> = AtomicReference()
        object : XmlXppAbstractParser() {
            override fun getRootHandlers(): List<XmlHandler> {
                return Arrays.asList(elementsPath(object : Handler {
                    override fun processElement(reader: XmlElementInfo): XmlReturn {
                        val stms = reader.getAttribute("CoveredStatements")
                        val total = reader.getAttribute("TotalStatements")
                        entry.set(Entry(parseInt(total!!, -1), parseInt(stms!!, -1)))
                        return reader.noDeep()
                    }
                }, "Root"))
            }
        }.parse(reportFile)
        return entry.get()
    }

    fun generateReportHTMLandStats(buildLogger: BuildProgressLogger,
                                   configParameters: Map<String, String>,
                                   checkoutDir: File?,
                                   dotCoverReport: File,
                                   destFile: File): CoverageStatistics? {
        checkoutDir ?: return null

        buildLogger.activityStarted("Generate dotCover HTML report", "dotCoverReport")

        try {
            val data: DotCoverData = loadCoverageModel(checkoutDir, dotCoverReport, buildLogger) ?: return null

            data.preprocessFoundFiles(buildLogger, configParameters)

            return callReportGenerator(dotCoverReport, destFile, buildLogger, data)
        } finally {
            buildLogger.activityFinished("Generate dotCover HTML report", "dotCoverReport")
        }
    }

    fun loadCoverageModel(checkoutDir: File, dotCoverReport: File, log: BuildProgressLogger): DotCoverData? {
        log.activityStarted("Loading dotCover report file...", "dotCoverReportLoad")
        val wd = WatchDog("Loading dotCover data from: " + dotCoverReport + " of size " + StringUtil.formatFileSize(dotCoverReport.length()))
        return try {
            buildCoverageData(checkoutDir, dotCoverReport)
        } catch (e: Throwable) {
            val message: String = "Failed to read dotCover report from: " + dotCoverReport + ". " + e.message
            LOG.warn(message, e)
            log.error(message)
            null
        } finally {
            wd.stop()
            log.activityFinished("Loading dotCover report file...", "dotCoverReportLoad")
        }
    }

    private fun callReportGenerator(dotCoverReport: File,
                                    destFile: File,
                                    log: BuildProgressLogger,
                                    data: CoverageData): CoverageStatistics? {

        log.activityStarted("Generating dotCover HTML report...", "dotCoverReportGen")
        try {
            val repBuilder: HTMLReportBuilder = ReportBuilderFactory.createHTMLReportBuilderForDotNet()
            val calc: StatisticsCalculator = ReportBuilderFactory.createStatisticsCalculator()

            repBuilder.setReportZip(destFile)
            val link = "<a href='http://www.jetbrains.com/dotcover/?utm_source=teamcity_server&utm_medium=report&utm_campaign=dotcover' target='_blank' rel='noreferrer'>"
            val poweredBy: String = "Powered by " + link + "JetBrains dotCover</a><br />"

            repBuilder.setFooterHTML(poweredBy + "Coverage highlighting right in Visual Studio, integration with ReSharper's unit testing tools")
            repBuilder.setSourceCodeFooterHTML(
                (poweredBy + "Do you want this kind of coverage highlighting right in Visual Studio? " +
                        "Get " + link + "dotCover</a> that is tightly integrated with both Visual Studio and ReSharper"))

            repBuilder.generateReport(data, calc)

            return calc.overallStats
        } catch (e: Throwable) {
            val message: String = "Failed to generate HTML report from: " + dotCoverReport + ". " + e.message
            LOG.warn(message, e)
            log.error(message)
        } finally {
            log.activityFinished("Generating dotCover HTML report...", "dotCoverReportGen")
        }
        return null
    }

    @Throws(IOException::class)
    private fun buildCoverageData(checkoutDir: File, file: File): DotCoverData? {
        if (!file.isFile || file.length() < 10) return null
        val data = DotCoverData(checkoutDir)
        object : XmlXppAbstractParser() {
            override fun getRootHandlers(): List<XmlHandler> {
                    return Arrays.asList(
                        elementsPath(object : Handler {
                            override fun processElement(reader: XmlElementInfo): XmlReturn {
                                return reader.visitChildren(
                                    elementsPath(object : Handler {
                                        override fun processElement(reader: XmlElementInfo): XmlReturn {
                                            val name: String = reader.getAttribute("Name") ?: ""
                                            val index: Int = reader.getAttribute("Index")?.toInt() ?: -1
                                            val path = File(name)
                                            if (index > 0) {
                                                data.filesMap.addFile(index, path)
                                            }
                                            return reader.noDeep()
                                        }
                                    }, "File"),
                                    elementsPath(object : Handler {
                                        override fun processElement(reader: XmlElementInfo): XmlReturn {
                                            val assemblyName: String? = reader.getAttribute("Name")
                                            if (assemblyName == null || StringUtil.isEmptyOrSpaces(assemblyName)) return reader.noDeep()
                                            val assembly = AssemblyInfo(assemblyName)
                                            return reader.visitChildren(
                                                processType(assembly.newNamespace(""), data),
                                                elementsPath(
                                                    object : Handler {
                                                        override fun processElement(reader: XmlElementInfo): XmlReturn {
                                                            val namespaceName: String? = reader.getAttribute("Name")
                                                            if (namespaceName == null || StringUtil.isEmptyOrSpaces(namespaceName))
                                                                return reader.noDeep()
                                                            return reader.visitChildren(
                                                                processType(assembly.newNamespace(namespaceName), data)
                                                            )
                                                        }
                                                    }, "Namespace"
                                                )
                                            )
                                        }
                                    }, "Assembly")
                                )
                            }
                        }, "Root")
                    )
                }

            private fun processType(namespace: NamespaceInfo,
                                    data: ClassHolder): XmlHandler {
                return elementsPath(
                    object : Handler {
                        override fun processElement(reader: XmlElementInfo): XmlReturn {
                            val typeName: String? = reader.getAttribute("Name")
                            if (typeName == null || StringUtil.isEmptyOrSpaces(typeName)) return reader.noDeep()
                            val clazz = DotCoverClass(namespace, typeName, data.getCoveredFiles())

                            val visitedMethods: MutableSet<String> = HashSet()
                            val totalMethods: MutableSet<String> = HashSet()

                            return reader.visitChildren(
                                processType(clazz.asNamespace(), clazz),
                                elementsPath(
                                    object : Handler {
                                        override fun processElement(reader: XmlElementInfo): XmlReturn {
                                            val methodName: String? = reader.getAttribute("Name")
                                            val covered: Int = reader.getAttribute("CoveredStatements")?.toInt() ?: 0
                                            val total: Int = reader.getAttribute("TotalStatements")?.toInt() ?: 0
                                            if ((methodName == null) || StringUtil.isEmptyOrSpaces(methodName) || (total <= 0)) return reader.noDeep()
                                            clazz.addStatementCoverage(total, covered)
                                            totalMethods.add(methodName)
                                            if (covered > 0) visitedMethods.add(methodName)
                                            return reader.visitChildren(
                                                elementsPath(
                                                    object : Handler {
                                                        override fun processElement(reader: XmlElementInfo): XmlReturn {
                                                            val fileId: Int = reader.getAttribute("FileIndex")?.toInt() ?: -1
                                                            val lineStart: Int = reader.getAttribute("Line")?.toInt() ?: -1
                                                            val lineEnd: Int = reader.getAttribute("EndLine")?.toInt() ?: -1
                                                            val isCovered: Boolean = "True".equals(
                                                                reader.getAttribute("Covered"),
                                                                ignoreCase = true)
                                                            if ((lineStart >= 0) && (lineEnd >= 0) && (fileId >= 0)) {
                                                                for (i in lineStart..lineEnd) {
                                                                    clazz.getCoveredFiles().addLine(fileId, i, isCovered)
                                                                }
                                                            }
                                                            return reader.noDeep()
                                                        }
                                                    }, "Statement"
                                                )
                                            )
                                        }
                                    }, "Member"
                                )
                            ).than(object : XmlAction {
                                override fun apply() {
                                    clazz.setMethodsCoverage(totalMethods.size, visitedMethods.size)
                                    data.addClassInfo(clazz)
                                }
                            })
                        }
                    }, "Type"
                )
            }
        }.parse(file)
        return data
    }

    private fun parseInt(key: String, def: Int): Int {
        return try {
            key.toInt()
        } catch (t: Throwable) {
            def
        }
    }

    companion object {
        private val LOG: Logger = Logger.getInstance(DotCoverTeamCityReportGenerator::class.java.name)
    }
}

