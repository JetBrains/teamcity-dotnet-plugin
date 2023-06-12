package jetbrains.buildServer.dotcover.report.model

class AssemblyInfo(private val _assemblyName: String) {

    val assemblyName: String
        get() = _assemblyName

    fun newNamespace(name: String): NamespaceInfo {
        return NamespaceInfo(this, name)
    }
}
