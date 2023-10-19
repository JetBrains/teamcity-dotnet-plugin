package jetbrains.buildServer.dotnet.coverage

import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.dotnet.coverage.serviceMessage.DotnetCoverageParameters
import java.io.File

class DotnetCoverageReportsMerger(private val _generators: DotnetCoverageReportGeneratorsHolder) {

    fun prepareReports(type: String, files: List<DotnetCoverageReportRequest>): DotnetCoverageGeneratorInput? {

        if (files.isEmpty()) return null
        val log: BuildProgressLogger = files.iterator().next().snapshot.getBuildLogger()

        val gen: DotnetCoverageReportGenerator? = _generators.getReportGenerator(type)
        if (gen == null) {
            log.warning("There is no .NET Coverage report generator for type '$type'. Skipped files: $files")
            return null
        }

        val rejected: MutableList<DotnetCoverageReportRequest> = ArrayList<DotnetCoverageReportRequest>()
        val accepted: MutableList<DotnetCoverageReportRequest> = ArrayList<DotnetCoverageReportRequest>()
        for (file: DotnetCoverageReportRequest in files) {
            if (!gen.supportCoverage(file.snapshot)) {
                rejected.add(file)
            } else {
                accepted.add(file)
            }
        }

        if (!rejected.isEmpty()) {
            log.warning("No available .NET Coverage report generator for type '$type'. Skipped files: $rejected")
        }

        val requests: List<DotnetCoverageReportRequest> = filterSameParameters(gen, accepted)
        if (requests.isEmpty()) return null

        if (requests.size > 1) {
            log.warning(
                "There are several .NET Coverage reports of type '" + type + "' with " +
                        "different parameters. Will use first parameters to generate all reports."
            )
            log.activityStarted(".NET Coverage reports of type '$type", ".NETCoverage")
            for (req: DotnetCoverageReportRequest in accepted) {
                log.message(("Report: " + req.reportFile) + ", " + gen.presentParameters(req.snapshot))
            }
            log.activityFinished(".NET Coverage reports of type '$type", ".NETCoverage")
        }

        val params: MutableList<DotnetCoverageParameters> = ArrayList()
        for (req: DotnetCoverageReportRequest in requests) {
            params.add(req.snapshot)
        }

        val pfiles: MutableList<File> = ArrayList()
        for (file: DotnetCoverageReportRequest in files) {
            pfiles.add(file.reportFile)
        }

        return DotnetCoverageGeneratorInput(gen, type, params, pfiles)
    }

    private fun filterSameParameters(gen: DotnetCoverageReportGenerator,
                                     reqs: List<DotnetCoverageReportRequest>): List<DotnetCoverageReportRequest> {
        val result: MutableList<DotnetCoverageReportRequest> = ArrayList()
        for (req: DotnetCoverageReportRequest in reqs) {
            var isFound = false
            for (res: DotnetCoverageReportRequest in result) {
                if (gen.parametersEquals(res.snapshot, req.snapshot)) {
                    isFound = true
                    break
                }
            }
            if (!isFound) {
                result.add(req)
            }
        }
        return result
    }
}
