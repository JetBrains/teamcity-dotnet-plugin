package jetbrains.buildServer.inspect

import jetbrains.buildServer.E

interface PackagesProvider {
    fun getPackages(specifications: String): E
}