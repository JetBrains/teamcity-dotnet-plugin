package jetbrains.buildServer.dotCover

import jetbrains.buildServer.ArtifactsConstants
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.web.openapi.ArtifactsViewTab
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.ReportTabsIsolationProtection
import jetbrains.buildServer.web.reportTabs.ReportTabUtil
import javax.servlet.http.HttpServletRequest

class DotCoverReportTab(
    pagePlaces: PagePlaces,
    server: SBuildServer,
    reportTabsIsolationProtection: ReportTabsIsolationProtection
) : ArtifactsViewTab("Code Coverage", "coverage_dotnet", pagePlaces, server, reportTabsIsolationProtection) {
    private val startPage = ArtifactsConstants.TEAMCITY_ARTIFACTS_DIR + "/.NETCoverage/coverage.zip!/index.html"

    init {
        includeUrl = "/artifactsViewer.jsp"
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest, build: SBuild) {
        super.fillModel(model, request, build)
        model["startPage"] = ReportTabUtil.prepareStartPageForWeb(startPage)
    }

    override fun isAvailable(request: HttpServletRequest, build: SBuild): Boolean {
        return super.isAvailable(request, build) && ReportTabUtil.isAvailable(build, startPage)
    }
}