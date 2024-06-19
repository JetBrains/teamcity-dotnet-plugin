package jetbrains.buildServer.dotnet.test.nunit

import io.mockk.MockKAnnotations
import io.mockk.clearAllMocks
import jetbrains.buildServer.nunit.toolState.NUnitToolState
import jetbrains.buildServer.nunit.toolState.NUnitToolStateParser
import org.testng.Assert
import org.testng.annotations.BeforeMethod
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class NUnitToolStateParserTest {
    @BeforeMethod
    fun setUp() {
        MockKAnnotations.init(this)
        clearAllMocks()
    }

    data class TestCase(val consoleOut: String, val expectedState: NUnitToolState)

    @DataProvider(name = "nUnit3Cases")
    fun getCommandLineArgumentsTryInitializeCases() = arrayOf(
        TestCase(
            """
            NUnit Console 3.17.0+685c5b542b5e9ba632c905f0bd514a773d9758af (Release)
            Copyright (c) 2022 Charlie Poole, Rob Prouse
            Friday, June 7, 2024 2:44:52 PM
            
            Runtime Environment
               OS Version: MacOSX 23.4.0.0
               Runtime: .NET Framework CLR v4.0.30319.42000
            
            Installed Extensions
              Extension Point: /NUnit/Engine/NUnitV2Driver
                Extension: NUnit.Engine.Drivers.NUnit2FrameworkDriver(.NET 2.0)
                  Version: 3.9.0.0
                  Path: /Users/Vladislav.Ma-iu-shan/Downloads/NUnit.Console-3.17.0 (3)/bin/net35/addins/nunit.v2.driver.dll
              Extension Point: /NUnit/Engine/TypeExtensions/IService
              Extension Point: /NUnit/Engine/TypeExtensions/ITestEventListener
                Extension: NUnit.Engine.Listeners.TeamCityEventListener(.NET 2.0)
                  Version: 1.0.8.0
                  Path: /Users/Vladislav.Ma-iu-shan/Downloads/NUnit.Console-3.17.0 (3)/bin/net35/addins/teamcity-event-listener.dll
              Extension Point: /NUnit/Engine/TypeExtensions/IDriverFactory
              Extension Point: /NUnit/Engine/TypeExtensions/IProjectLoader
                Extension: NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader(.NET 2.0)
                  Version: 3.7.0.0
                  Path: /Users/Vladislav.Ma-iu-shan/Downloads/NUnit.Console-3.17.0 (3)/bin/net35/addins/nunit-project-loader.dll
                  FileExtension: .nunit
                Extension: NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader(.NET 2.0)
                  Version: 3.9.0.0
                  Path: /Users/Vladislav.Ma-iu-shan/Downloads/NUnit.Console-3.17.0 (3)/bin/net35/addins/vs-project-loader.dll
                  FileExtension: .sln .csproj .vbproj .vjsproj .vcproj .fsproj
              Extension Point: /NUnit/Engine/TypeExtensions/IResultWriter
                Extension: NUnit.Engine.Addins.NUnit2XmlResultWriter(.NET 2.0)
                  Version: 3.6.0.0
                  Path: /Users/Vladislav.Ma-iu-shan/Downloads/NUnit.Console-3.17.0 (3)/bin/net35/addins/nunit-v2-result-writer.dll
                  Format: nunit2
            """.trimIndent(),
            NUnitToolState(
                "3.17.0", listOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            )
        ),
        TestCase(
            """NUnit Console 3.15.4 (Release)
            Copyright (c) 2022 Charlie Poole, Rob Prouse
            Friday, June 7, 2024 2:48:47 PM
            """.trimIndent(),
            NUnitToolState("3.15.4", emptyList())
        ),
        TestCase(
            """
            NUnit Console Runner 3.13.2 (.NET 2.0)
            Copyright (c) 2021 Charlie Poole, Rob Prouse
            Friday, June 7, 2024 2:50:57 PM
            
            Runtime Environment
               OS Version: MacOSX 23.4.0.0
               Runtime: .NET Framework CLR v4.0.30319.42000
            """.trimIndent(),
            NUnitToolState("3.13.2", emptyList())
        ),
        TestCase(
            """NUnit Console Runner 3.4.1
            Copyright (C) 2016 Charlie Poole
            
            Runtime Environment
               OS Version: Microsoft Windows NT 10.0.14393.0
              CLR Version: 4.0.30319.42000
            
            Installed Extensions
              Extension Point: /NUnit/Engine/NUnitV2Driver
                Extension: NUnit.Engine.Drivers.NUnit2FrameworkDriver
              Extension Point: /NUnit/Engine/TypeExtensions/IService
              Extension Point: /NUnit/Engine/TypeExtensions/ITestEventListener
                Extension: NUnit.Engine.Listeners.TeamCityEventListener
              Extension Point: /NUnit/Engine/TypeExtensions/IDriverFactory
              Extension Point: /NUnit/Engine/TypeExtensions/IProjectLoader
                Extension: NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader
                  FileExtension: .nunit
                Extension: NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader
                  FileExtension: .sln .csproj .vbproj .vjsproj .vcproj .fsproj
              Extension Point: /NUnit/Engine/TypeExtensions/IResultWriter
                Extension: NUnit.Engine.Addins.NUnit2XmlResultWriter
                  Format: nunit2
            """.trimIndent(),
            NUnitToolState(
                "3.4.1",
                mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            )
        ),

        TestCase(
            ("""NUnit Console Runner 3.9.0
                Copyright (c) 2018 Charlie Poole, Rob Prouse
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.17134.0
                  CLR Version: 4.0.30319.42000
                
                Installed Extensions
                  Extension Point: /NUnit/Engine/NUnitV2Driver
                    Extension: NUnit.Engine.Drivers.NUnit2FrameworkDriver (.NET 2.0)
                  Extension Point: /NUnit/Engine/TypeExtensions/IService
                  Extension Point: /NUnit/Engine/TypeExtensions/ITestEventListener
                    Extension: NUnit.Engine.Listeners.TeamCityEventListener (.NET 2.0) (Disabled)
                  Extension Point: /NUnit/Engine/TypeExtensions/IDriverFactory
                  Extension Point: /NUnit/Engine/TypeExtensions/IProjectLoader
                    Extension: NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader (.NET 2.0)
                      FileExtension: .nunit
                    Extension: NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader (.NET 2.0)
                      FileExtension: .sln .csproj .vbproj .vjsproj .vcproj .fsproj
                  Extension Point: /NUnit/Engine/TypeExtensions/IResultWriter
                    Extension: NUnit.Engine.Addins.NUnit2XmlResultWriter (.NET 2.0)
                      Format: nunit2

            """).trimIndent(),
            NUnitToolState(
                "3.9.0",
                mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            )
        ),

        TestCase(
            ("""NUnit Console Runner 3.4.1.2.3.4
                Copyright (C) 2016 Charlie Poole
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.14393.0
                  CLR Version: 4.0.30319.42000
                
                Installed Extensions
                  Extension Point: /NUnit/Engine/NUnitV2Driver
                    Extension: NUnit.Engine.Drivers.NUnit2FrameworkDriver
                  Extension Point: /NUnit/Engine/TypeExtensions/IService
                  Extension Point: /NUnit/Engine/TypeExtensions/ITestEventListener
                    Extension: NUnit.Engine.Listeners.TeamCityEventListener
                  Extension Point: /NUnit/Engine/TypeExtensions/IDriverFactory
                  Extension Point: /NUnit/Engine/TypeExtensions/IProjectLoader
                    Extension: NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader
                      FileExtension: .nunit
                    Extension: NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader
                      FileExtension: .sln .csproj .vbproj .vjsproj .vcproj .fsproj
                  Extension Point: /NUnit/Engine/TypeExtensions/IResultWriter
                    Extension: NUnit.Engine.Addins.NUnit2XmlResultWriter
                      Format: nunit2
            """).trimIndent(),
            NUnitToolState(
                "3.4.1",
                mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            )
        ),

        TestCase(
            ("""NUnit Console Runner 3.4.1
                Copyright (C) 2016 Charlie Poole
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.14393.0
                  CLR Version: 4.0.30319.42000
                
                Extension: NUnit.Engine.Listeners.TeamCityEventListener
                """).trimIndent(),
            NUnitToolState("3.4.1", mutableListOf("NUnit.Engine.Listeners.TeamCityEventListener"))
        ),
        TestCase(
            ("""NUnit Console Runner 3.5
                Copyright (C) 2016 Charlie Poole
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.14393.0
                  CLR Version: 4.0.30319.42000
                
                Installed Extensions
                  Extension Point: /NUnit/Engine/NUnitV2Driver
                    Extension: NUnit.Engine.Drivers.NUnit2FrameworkDriver
                  Extension Point: /NUnit/Engine/TypeExtensions/IService
                  Extension Point: /NUnit/Engine/TypeExtensions/ITestEventListener
                    Extension: NUnit.Engine.Listeners.TeamCityEventListener
                  Extension Point: /NUnit/Engine/TypeExtensions/IDriverFactory
                  Extension Point: /NUnit/Engine/TypeExtensions/IProjectLoader
                    Extension: NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader
                      FileExtension: .nunit
                    Extension: NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader
                      FileExtension: .sln .csproj .vbproj .vjsproj .vcproj .fsproj
                  Extension Point: /NUnit/Engine/TypeExtensions/IResultWriter
                    Extension: NUnit.Engine.Addins.NUnit2XmlResultWriter
                      Format: nunit2
            """).trimIndent(),
            NUnitToolState(
                "3.5",
                mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            )
        ),
        TestCase(
            ("""NUnit Console Runner 3.2.1
                Copyright (C) 2016 Charlie Poole
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.14393.0
                  CLR Version: 4.0.30319.42000
                
                Installed Extensions
                  Extension Point: /NUnit/Engine/NUnitV2Driver
                    Extension: NUnit.Engine.Drivers.NUnit2FrameworkDriver
                  Extension Point: /NUnit/Engine/TypeExtensions/IService
                  Extension Point: /NUnit/Engine/TypeExtensions/ITestEventListener
                    Extension: NUnit.Engine.Listeners.TeamCityEventListener
                  Extension Point: /NUnit/Engine/TypeExtensions/IDriverFactory
                  Extension Point: /NUnit/Engine/TypeExtensions/IProjectLoader
                    Extension: NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader
                      FileExtension: .nunit
                    Extension: NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader
                      FileExtension: .sln .csproj .vbproj .vjsproj .vcproj .fsproj
                  Extension Point: /NUnit/Engine/TypeExtensions/IResultWriter
                    Extension: NUnit.Engine.Addins.NUnit2XmlResultWriter
                      Format: nunit2
                """).trimIndent(),
            NUnitToolState(
                "3.2.1",
                mutableListOf(
                    "NUnit.Engine.Drivers.NUnit2FrameworkDriver",
                    "NUnit.Engine.Listeners.TeamCityEventListener",
                    "NUnit.Engine.Services.ProjectLoaders.NUnitProjectLoader",
                    "NUnit.Engine.Services.ProjectLoaders.VisualStudioProjectLoader",
                    "NUnit.Engine.Addins.NUnit2XmlResultWriter"
                )
            )
        ),
        TestCase(
            ("""NUnit Console Runner 3.4.1
                Copyright (C) 2016 Charlie Poole
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.14393.0
                  CLR Version: 4.0.30319.42000

            """).trimIndent(),
            NUnitToolState("3.4.1", mutableListOf())
        ),
        TestCase(
            ("""NUnit Console Runner 3.4.1
                Copyright (C) 2016 Charlie Poole
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.14393.0
                  CLR Version: 4.0.30319.42000
                
                """).trimIndent(),
            NUnitToolState("3.4.1", mutableListOf())
        ),
        TestCase(
            ("""NUnit Console Runner
                Copyright (C) 2016 Charlie Poole
                
                Runtime Environment
                   OS Version: Microsoft Windows NT 10.0.14393.0
                  CLR Version: 4.0.30319.42000
           
            """).trimIndent(),
            NUnitToolState("", mutableListOf())
        ),
    )

    @Test(dataProvider = "nUnit3Cases")
    fun `should parse nunit console output`(testCase: TestCase) {
        // arrange
        val parser = NUnitToolStateParser()

        // act
        val state = parser.parse(0, testCase.consoleOut.split("\n"))

        // assert
        Assert.assertEquals(state, testCase.expectedState)
    }
}