package jetbrains.buildServer.nunit.testReordering

import java.io.File

data class TestInfo(val assembly: File?, val className: String, val fullMethodName: String?) {
    constructor(className: String) : this(assembly = null, className = className, fullMethodName = null)
}