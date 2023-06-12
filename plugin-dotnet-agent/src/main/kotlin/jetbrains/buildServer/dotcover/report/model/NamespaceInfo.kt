package jetbrains.buildServer.dotcover.report.model

open class NamespaceInfo(val assembly: AssemblyInfo,
                         val namespaceName: String) {

    open fun makeClassName(name: String): String {
        return name
    }

    open fun forClass(clazz: DotCoverClass): NamespaceInfo {
        val namePrefix = clazz.name + "+"
        return object :
            NamespaceInfo(assembly, clazz.namespace) {
            override fun makeClassName(name: String): String {
                return namePrefix + name
            }
        }
    }
}

