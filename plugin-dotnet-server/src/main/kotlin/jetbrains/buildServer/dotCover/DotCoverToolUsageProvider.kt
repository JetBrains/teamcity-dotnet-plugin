package jetbrains.buildServer.dotCover

import jetbrains.buildServer.dotnet.CoverageConstants
import jetbrains.buildServer.serverSide.BuildPromotionEx
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.serverSide.SettingDescription
import jetbrains.buildServer.tools.*

class DotCoverToolUsageProvider(
    private val _defaultToolFinder: DefaultToolFinder,
    private val _serverToolManager: ServerToolManager,
    private val _toolType: ToolType
) : ToolUsagesProvider, IgnoredParametersKeys {

    override fun getRequiredTools(build: SRunningBuild): List<ToolVersion> {
        val bp = build.buildPromotion as? BuildPromotionEx ?: return emptyList()

        val buildPromotion: BuildPromotionEx = bp

        val result: MutableList<ToolVersion> = ArrayList()

        if (isUsedBundledToolWithNotDefaultParams(buildPromotion)) {
            val bundledTool = _serverToolManager.findInstalledTool(CoverageConstants.DOTCOVER_BUNDLED_TOOL_ID)
            if (bundledTool != null) {
                result.add(bundledTool)
            }
        }

        for (runnerDescriptor in buildPromotion.getBuildSettings().getAllBuildRunners().getEnabledBuildRunners()) {
            //dot cover tool is required only if dot cover is enabled
            val coverageType = runnerDescriptor.parameters.getOrDefault(CoverageConstants.PARAM_TYPE, "")
            if (coverageType.isEmpty()) continue
            if (CoverageConstants.PARAM_DOTCOVER != coverageType) continue

            val selectedTool = runnerDescriptor.parameters.getOrDefault(CoverageConstants.PARAM_DOTCOVER_HOME, "")
            if (selectedTool.isEmpty() || ToolVersionReference.isDefaultToolReference(selectedTool)) {
                val defaultTool = _defaultToolFinder.getDefaultTool(_toolType, build)
                if (defaultTool != null) {
                    result.add(defaultTool)
                }
                continue
            }

            val toolId = ToolVersionReference.resolveToolId(selectedTool) ?: continue
            val installedTool = _serverToolManager.findInstalledTool(toolId)
            if (installedTool != null) {
                result.add(installedTool)
            }
        }

        return result
    }

    //dot cover also add two params with reference to bundled dot cover tool.
    //see DotCoverPropertiesExtension.java
    private fun isUsedBundledToolWithNotDefaultParams(buildPromotion: BuildPromotionEx): Boolean {
        val referencedParameters: Map<String, SettingDescription> = buildPromotion.buildSettings.referencedParameters
        for (ref in BUNDLED_DOT_COVER_REFERENCES) {
            if (referencedParameters.containsKey(ref)) {
                return true
            }
        }
        return false
    }

    override fun getIgnoredParametersKeys(): Collection<String> {
        return setOf(CoverageConstants.PARAM_DOTCOVER_HOME)
    }

    companion object {
        private val BUNDLED_DOT_COVER_REFERENCES = arrayOf(
            ToolVersionReference.TOOL_PARAMETER_PREFIX + CoverageConstants.DOTCOVER_TOOL_NAME,
            CoverageConstants.TEAMCITY_DOTCOVER_HOME
        )
    }
}
