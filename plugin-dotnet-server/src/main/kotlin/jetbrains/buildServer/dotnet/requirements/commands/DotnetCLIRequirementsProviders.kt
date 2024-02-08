package jetbrains.buildServer.dotnet.requirements.commands

import jetbrains.buildServer.RequirementsProvider
import jetbrains.buildServer.dotnet.DotnetCommandType
import jetbrains.buildServer.dotnet.DotnetConstants
import jetbrains.buildServer.requirements.Requirement
import jetbrains.buildServer.requirements.RequirementType

abstract class DotnetCLIRequirementsProvider : RequirementsProvider {
    override fun getRequirements(parameters: Map<String, String>) = sequence {
        yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI_PATH, null, RequirementType.EXISTS))
    }
}

class BuildRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Build
}

class CleanRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Clean
}

class CustomRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Custom
}

class NugetDeleteRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.NuGetDelete
}

class NugetPushRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.NuGetPush
}

class PackRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Pack

    override fun getRequirements(parameters: Map<String, String>) = sequence {
        if (!parameters[DotnetConstants.PARAM_RUNTIME].isNullOrBlank()) {
            yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI, "2.0.0", RequirementType.VER_NO_LESS_THAN))
        }
    }
}

class PublishRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Publish
}

class RestoreRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Restore
}

class RunRequirementsProvider : DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Run

    override fun getRequirements(parameters: Map<String, String>) = sequence {
        if (!parameters[DotnetConstants.PARAM_RUNTIME].isNullOrBlank()) {
            yield(Requirement(DotnetConstants.CONFIG_SUFFIX_DOTNET_CLI, "2.0.0", RequirementType.VER_NO_LESS_THAN))
        }
    }
}

class TestRequirementsProvider : DotnetCLIRequirementsProvider(), DotnetCommandRequirementsProvider {
    override val commandType = DotnetCommandType.Test
}
