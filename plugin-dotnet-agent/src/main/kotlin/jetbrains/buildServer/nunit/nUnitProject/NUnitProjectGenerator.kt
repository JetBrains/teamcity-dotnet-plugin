package jetbrains.buildServer.nunit.nUnitProject

import jetbrains.buildServer.nunit.arguments.NUnitTestingAssembliesProvider

class NUnitProjectGenerator(
    private val _testingAssembliesProvider: NUnitTestingAssembliesProvider
) {
    fun generate(): List<NUnitProject> {
        val testingAssemblies = _testingAssembliesProvider.assemblies
        if (testingAssemblies.isEmpty()) {
            return emptyList()
        }

        // Several nunit project file - splits assemblies by baseDir
        return testingAssemblies
            .map { it.absoluteFile }
            .groupBy { it.parentFile }
            .map { assemblyGroup -> NUnitProject(assemblyGroup.key, assemblyGroup.value) }
    }
}
