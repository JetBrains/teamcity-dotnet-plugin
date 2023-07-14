package jetbrains.buildServer.healthStatus.reports

import jetbrains.buildServer.inspect.CltConstants
import jetbrains.buildServer.inspect.DupFinderConstants
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.healthStatus.*
import jetbrains.buildServer.serverSide.healthStatus.reports.BuildTypeSettingsReport

class DupFinderWithIncompatibleCltVersionReport : BuildTypeSettingsReport() {
    private val itemCategory = ItemCategory(
        "dupfinder_runner_with_incompatible_clt_usage",
        "Duplicates Finder (ReSharper) runner with the bundled JetBrains ReSharper CLT is used",
        ItemSeverity.WARN,
        "Detects build configurations that utilize Duplicates Finder (ReSharper) runners with the bundled JetBrains ReSharper Command Line Tools.",
        null
    )

    override fun getType(): String = Type

    override fun getDisplayName(): String = "Find Duplicates Finder (ReSharper) runners that use bundled JetBrains ReSharper Command Line Tools"

    override fun getCategories(): List<ItemCategory> = listOf(itemCategory)

    override fun canReportItemsFor(scope: HealthStatusScope): Boolean = scope.isItemWithSeverityAccepted(ItemSeverity.WARN)

    override fun reportProblems(buildType: SBuildType, resultConsumer: HealthStatusItemConsumer) {
        val dupFinderRunners = buildType.buildRunners.filter { it.type == DupFinderConstants.RUNNER_TYPE }

        val runnersWithBundledCltVersion = mutableListOf<SBuildRunnerDescriptor>()
        dupFinderRunners.forEach {
            val cltPath = it.parameters[CltConstants.CLT_PATH_PARAMETER] ?: return@forEach
            if (cltPath == BundledCltPath) {
                runnersWithBundledCltVersion.add(it)
            }
        }

        if (runnersWithBundledCltVersion.isEmpty()) {
            return
        }

        val additionalData = HashMap<String, Any>()
        additionalData["buildType"] = buildType
        val runnerIds = runnersWithBundledCltVersion.map { it.id }
        additionalData["runnerIds"] = runnerIds
        val runnerIdsString = runnerIds.sorted().joinToString("_")
        val identity = "dupfinder_runner_with_incompatible_clt_buildType_${buildType.internalId}_runners_$runnerIdsString"
        resultConsumer.consumeForBuildType(buildType, HealthStatusItem(identity, itemCategory, additionalData))
    }

    companion object {
        const val Type: String = "DupFinderWithIncompatibleCltUsage"
        private const val BundledCltPath: String = "%teamcity.tool.jetbrains.resharper-clt.bundled%"
    }
}
