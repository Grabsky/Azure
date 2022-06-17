val buildsDirectory = "${System.getenv("DEVELOPMENT_DIR")}/builds"

group = "azure"
version = "1.0-SNAPSHOT"

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.0"
    id("io.papermc.paperweight.userdev") version "1.3.6"
}

repositories {
    maven("https://jitpack.io")
    maven("https://papermc.io/repo/repository/maven-public/")
}

dependencies {
    // Kotlin
    compileOnly("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.7.0")
    compileOnly("org.jetbrains.kotlinx", "kotlinx-serialization-core", "1.3.3")
    compileOnly("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.3")
    // Paper
    paperDevBundle("1.18.2-R0.1-SNAPSHOT")
    // Dependencies
    compileOnly("net.luckperms", "api", "5.4")
    compileOnly(files(buildsDirectory + File.separator + "Indigo.jar"))
}

tasks {
    assemble {
        dependsOn(reobfJar)
        doLast {
            // Copying output file to builds directory
            copy {
                from (reobfJar)
                into(buildsDirectory)
                // Renaming output file
                rename(reobfJar.get().outputJar.asFile.get().name, "${rootProject.name}.jar")
            }
        }
    }
    compileKotlin { kotlinOptions.javaParameters = true }
    processResources { filteringCharset = Charsets.UTF_8.name() }
}