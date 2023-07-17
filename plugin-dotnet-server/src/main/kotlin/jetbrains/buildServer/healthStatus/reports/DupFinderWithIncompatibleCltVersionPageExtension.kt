package jetbrains.buildServer.healthStatus.reports

import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension
import jetbrains.buildServer.web.util.SessionUser
import javax.servlet.http.HttpServletRequest

class DupFinderWithIncompatibleCltVersionPageExtension(
    pagePlaces: PagePlaces,
    pluginDescriptor: PluginDescriptor
) : HealthStatusItemPageExtension(DupFinderWithIncompatibleCltVersionReport.Type, pagePlaces) {
    init {
        includeUrl = pluginDescriptor.getPluginResourcesPath("dupFinderWithIncompatibleCltVersionReport.jsp")
        register()
    }

    override fun isAvailable(request: HttpServletRequest): Boolean {
        if (!super.isAvailable(request)) return false

        val item = getStatusItem(request)
        val bt = item.additionalData["buildType"] as SBuildType? ?: return false

        return SessionUser.getUser(request).isPermissionGrantedForProject(bt.projectId, Permission.EDIT_PROJECT)
    }
}
