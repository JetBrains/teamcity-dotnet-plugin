package jetbrains.buildServer.dotnet.test.depcache

import jetbrains.buildServer.depcache.DotnetDepCacheStepContext
import org.testng.Assert
import org.testng.annotations.Test
import java.nio.file.Paths

class DotnetDepCacheStepContextTest {

    @Test
    fun `should increment execution number` () {
        // arrange
        val context = DotnetDepCacheStepContext.newContext()
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