package jetbrains.buildServer.nunit.nUnitProject

import java.io.File

data class NUnitProject(val appBase: File, val testingAssemblies: List<File>)
