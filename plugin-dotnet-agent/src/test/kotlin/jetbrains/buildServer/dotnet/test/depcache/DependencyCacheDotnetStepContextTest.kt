package jetbrains.buildServer.dotnet.test.depcache

import jetbrains.buildServer.depcache.DependencyCacheDotnetStepContext
import org.testng.Assert
import org.testng.annotations.Test
import java.nio.file.Paths

class DependencyCacheDotnetStepContextTest {

    @Test
    fun `should increment execution number` () {
        // arrange
        val context = DependencyCacheDotnetStepContext.newContext()
        var commandsCounter = 0

        // act
        var cacheRootUsage = context.newCacheRootUsage(Paths.get("path"), "stepId")

        // assert
        Assert.assertEquals(cacheRootUsage.descriptor, "stepId.$commandsCounter")

        // act
        commandsCounter++
        cacheRootUsage = context.newCacheRootUsage(Paths.get("path"), "stepId")

        // assert
        Assert.assertEquals(cacheRootUsage.descriptor, "stepId.$commandsCounter")
    }

}