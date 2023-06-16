package jetbrains.buildServer.dotcover.report.model

import jetbrains.coverage.report.ClassInfo
import jetbrains.coverage.report.Entry

class DotCoverClass(private val _namespace: NamespaceInfo,
                    name: String,
                    private val _coveredFiles: CoveredFiles) : ClassInfo, ClassHolder {

    private val _name: String
    private val _innerClasses = ArrayList<ClassInfo>(0)
    private val _methods = AdditiveValue()
    private val _statements = AdditiveValue()

    init {
        _name = _namespace.makeClassName(name)
    }

    override fun addClassInfo(info: ClassInfo) {
        _innerClasses.add(info)
    }

    override fun getModule(): String {
        return _namespace.assembly.assemblyName
    }

    fun setMethodsCoverage(total: Int, covered: Int) {
        _methods.increment(total, covered)
    }

    fun addStatementCoverage(total: Int, covered: Int) {
        _statements.increment(total, covered)
    }

    override fun getStatementStats(): Entry? {
        return _statements.entry
    }

    override fun getName(): String {
        return _name
    }

    override fun getNamespace(): String {
        return _namespace.namespaceName
    }

    override fun getFQName(): String {
        return "$namespace.$name"
    }

    override fun getMethodStats(): Entry {
        return _methods.entry
    }

    override fun getBlockStats(): Entry? {
        return null
    }

    override fun getLineStats(): Entry? {
        return null
    }

    override fun getInnerClasses(): Collection<ClassInfo> {
        return _innerClasses
    }

    fun asNamespace(): NamespaceInfo {
        return _namespace.forClass(this)
    }

    override fun getCoveredFiles(): CoveredFiles {
        return _coveredFiles
    }
}

