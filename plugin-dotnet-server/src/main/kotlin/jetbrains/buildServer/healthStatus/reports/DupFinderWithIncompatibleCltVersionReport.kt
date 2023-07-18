package jetbrains.buildServer.healthStatus.reports

import jetbrains.buildServer.ToolVersionProvider
import jetbrains.buildServer.inspect.CltConstants
import jetbrains.buildServer.inspect.DupFinderConstants
import jetbrains.buildServer.inspect.RequirementsResolverImpl
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.healthStatus.*
import jetbrains.buildServer.serverSide.healthStatus.reports.BuildTypeSettingsReport

class DupFinderWithIncompatibleCltVersionReport(private val _toolVersionProvider: ToolVersionProvider) : BuildTypeSettingsReport() {
    private val itemCategory = ItemCategory(
        "dupfinder_runner_with_incompatible_clt_usage",
        "Deprecated Duplicates Finder (ReSharper) runner using incompatible JetBrains ReSharper Command Line Tools",
        ItemSeverity.ERROR,
        "Detects usages of the deprecated Duplicates Finder (ReSharper) runner that utilizes an incompatible version of JetBrains ReSharper Command Line Tools",
        null
    )

    override fun getType(): String = Type

    override fun getDisplayName(): String = "Find deprecated Duplicates Finder (ReSharper) runners that use incompatible versions of JetBrains ReSharper Command Line Tools"

    override fun getCategories(): List<ItemCategory> = listOf(itemCategory)

    override fun canReportItemsFor(scope: HealthStatusScope): Boolean = scope.isItemWithSeverityAccepted(ItemSeverity.ERROR)

    override fun reportProblems(buildType: SBuildType, resultConsumer: HealthStatusItemConsumer) {
        val dupFinderRunners = buildType.buildRunners.filter { it.type == DupFinderConstants.RUNNER_TYPE }

        val runnersWithIncompatibleCltVersion = mutableListOf<SBuildRunnerDescriptor>()
        dupFinderRunners.forEach {
            val cltPath = it.parameters[CltConstants.CLT_PATH_PARAMETER] ?: return@forEach
            val cltVersion = _toolVersionProvider.getVersion(cltPath, CltConstants.JETBRAINS_RESHARPER_CLT_TOOL_TYPE_ID)

            if (cltVersion > RequirementsResolverImpl.LastVersionWithDupFinder) {
                runnersWithIncompatibleCltVersion.add(it)
            }
        }

        if (runnersWithIncompatibleCltVersion.isEmpty()) {
            return
        }

        val additionalData = HashMap<String, Any>()
        additionalData["buildType"] = buildType
        val runnerIds = runnersWithIncompatibleCltVersion.map { it.id }
        additionalData["runnerIds"] = runnerIds
        val runnerIdsString = runnerIds.sorted().joinToString("_")
        val identity = "dupfinder_runner_with_incompatible_clt_buildType_${buildType.internalId}_runners_$runnerIdsString"
        resultConsumer.consumeForBuildType(buildType, HealthStatusItem(identity, itemCategory, additionalData))
    }

    companion object {
        const val Type: String = "DupFinderWithIncompatibleCltUsage"
    }
}
