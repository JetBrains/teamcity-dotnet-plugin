package jetbrains.buildServer.dotnet.test.integration

import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.MountableFile
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeText

class DotnetSdkContainer(imageName: String) : GenericContainer<DotnetSdkContainer>(imageName) {
    lateinit var tempMount: Path

    override fun start() {
        withWorkingDirectory("/app")
        withEnv(mapOf("TEAMCITY_VERSION" to "2024.11"))

        // container volume to get test retry reports
        tempMount = createTempDirectory()
        val tempMountPath = tempMount.absolutePathString()
        withFileSystemBind(tempMountPath, tempMountPath, BindMode.READ_WRITE)

        // keep the container running for the test
        withCommand("tail", "-f", "/dev/null")

        super.start()
    }

    fun createFileInContainer(containerPath: String, content: String) {
        val file = Files.createTempFile("container", "tmp")
        try {
            file.writeText(content)
            copyFileToContainer(MountableFile.forHostPath(file.absolutePathString()), containerPath)
        } finally {
            file.deleteIfExists()
        }
    }
}