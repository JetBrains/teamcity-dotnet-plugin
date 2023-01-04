package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.web.openapi.ArtifactsViewTab
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.ReportTabsIsolationProtection
import jetbrains.buildServer.web.reportTabs.ReportTabUtil
import java.nio.file.Path
import javax.servlet.http.HttpServletRequest

class DotCoverNativeReportTab(
    pagePlaces: PagePlaces,
    server: SBuildServer,
    reportTabsIsolationProtection: ReportTabsIsolationProtection,
) : ArtifactsViewTab(TAB_TITLE, TAB_CODE, pagePlaces, server, reportTabsIsolationProtection) {
    init {
        includeUrl = "/artifactsViewer.jsp"
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest, build: SBuild) {
        super.fillModel(model, request, build)
        model["startPage"] = CoverageConstants.DOTCOVER_NATIVE_REPORT_FILE_PATH
    }

    override fun isAvailable(request: HttpServletRequest, build: SBuild) =
        super.isAvailable(request, build) && ReportTabUtil.isAvailable(build, CoverageConstants.DOTCOVER_NATIVE_REPORT_FILE_PATH)

    companion object {
        private const val TAB_TITLE = "dotCover Coverage"
        private const val TAB_CODE = "dotCoverNativeReportTab"
    }
}