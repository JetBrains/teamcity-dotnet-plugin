

package jetbrains.buildServer.dotnet.discovery

data class Project(
        val project: String,
        var configurations: List<Configuration> = emptyList(),
        var frameworks: List<Framework> = emptyList(),
        var runtimes: List<Runtime> = emptyList(),
        val references: List<Reference> = emptyList(),
        val targets: List<Target> = emptyList(),
        val generatePackageOnBuild: Boolean = false,
        val properties: List<Property> = listOf(Property("Sdk", "Microsoft.NET.Sdk")))