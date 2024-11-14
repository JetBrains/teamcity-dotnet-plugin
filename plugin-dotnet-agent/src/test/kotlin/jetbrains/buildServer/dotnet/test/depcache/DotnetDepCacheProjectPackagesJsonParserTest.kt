package jetbrains.buildServer.dotnet.test.depcache

import jetbrains.buildServer.depcache.utils.DotnetDepCacheProjectPackagesJsonParser
import org.testng.Assert
import org.testng.annotations.Test

class DotnetDepCacheProjectPackagesJsonParserTest {

    @Test
    fun `should parse from JSON string`() {
        // arrange
        val output = """
            {
              "version": 1,
              "parameters": "--include-transitive",
              "projects": [
                {
                  "path": "/Users/You/Projects/Prj/Prj1.csproj",
                  "frameworks": [
                    {
                      "framework": "net8.0",
                      "topLevelPackages": [
                        {
                          "id": "Serilog.Sinks.Console",
                          "requestedVersion": "6.0.0",
                          "resolvedVersion": "6.0.1"
                        }
                      ],
                      "transitivePackages": [
                        {
                          "id": "Serilog",
                          "resolvedVersion": "4.0.0"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        // act
        val result = DotnetDepCacheProjectPackagesJsonParser.fromCommandLineOutput(output)

        // assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(result.getOrThrow().projects?.size, 1)
        val project = result.getOrThrow().projects!!.get(0)
        Assert.assertEquals(project.path, "/Users/You/Projects/Prj/Prj1.csproj")
        Assert.assertEquals(project.frameworks?.size, 1)
        val framework = project.frameworks!!.get(0)
        Assert.assertEquals(framework.topLevelPackages?.size, 1)
        Assert.assertEquals(framework.transitivePackages?.size, 1)
        val topLevelPackage = framework.topLevelPackages!!.get(0)
        Assert.assertEquals(topLevelPackage.id, "Serilog.Sinks.Console")
        Assert.assertEquals(topLevelPackage.requestedVersion, "6.0.0")
        Assert.assertEquals(topLevelPackage.resolvedVersion, "6.0.1")
        val transitivePackage = framework.transitivePackages!!.get(0)
        Assert.assertEquals(transitivePackage.id, "Serilog")
        Assert.assertNull(transitivePackage.requestedVersion)
        Assert.assertEquals(transitivePackage.resolvedVersion, "4.0.0")
    }

    @Test
    fun `should parse problems from JSON string`() {
        // arrange
        val output = """
        {
          "version": 1,
          "parameters": "--include-transitive",
          "problems": [
            {
              "project": "/Users/You/Projects/Prj/Prj1.csproj",
              "level": "error",
              "text": "No assets file was found for `/Users/You/Projects/Prj/Prj1.csproj`. Please run restore before running this command."
            }
          ],
          "projects": [
            {
              "path": "/Users/You/Projects/Prj/Prj1.csproj"
            }
          ]
        }
        """.trimIndent()

        // act
        val result = DotnetDepCacheProjectPackagesJsonParser.fromCommandLineOutput(output)

        // assert
        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(result.getOrThrow().problems?.size, 1)
        val problem = result.getOrThrow().problems!!.get(0)
        Assert.assertEquals(problem.level, "error")
        Assert.assertEquals(problem.text, "No assets file was found for `/Users/You/Projects/Prj/Prj1.csproj`. Please run restore before running this command.")
    }

    @Test
    fun `should fail when invalid JSON is passed`() {
        // arrange
        val output = "invalid JSON"

        // act
        val result = DotnetDepCacheProjectPackagesJsonParser.fromCommandLineOutput(output)

        // assert
        Assert.assertTrue(result.isFailure)
    }
}