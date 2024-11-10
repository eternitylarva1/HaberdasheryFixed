plugins {
    `java-library`
    kotlin("jvm") version "1.7.22"
}

buildscript {
    extra.apply {
        mapOf(
            "id" to "haberdashery",
            "name" to "Haberdashery",
            "version" to "0.1",
            "sts_version" to "12-22-2020",
            "mts_version" to "3.21.0",
            "authors" to listOf("kiooeht").joinToString(separator = "\", \""),
            "dependencies" to listOf("basemod").joinToString(separator = "\", \""),
        ).forEach { (k, v) -> set(k, v) }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    val libDir = layout.projectDirectory.dir("..").dir("lib")
    implementation(files(
        libDir.file("desktop-1.0.jar"),
        libDir.file("ModTheSpire.jar"),
        libDir.file("BaseMod.jar"),
    ))
}

group = "com.evacipated.cardcrawl.mod"
version = extra["version"].toString()
description = extra["name"].toString()
java.sourceCompatibility = JavaVersion.VERSION_1_8

tasks.processResources {
    val expansion: FileCopyDetails.() -> Unit = {
        expand(
            "mod" to project.extra.properties,
        )
    }
    filesMatching("ModTheSpire.json", expansion)
    filesMatching("${project.extra["id"]}Assets/**/*.json", expansion)
}

tasks.jar {
    archiveBaseName = project.extra["name"].toString()
    archiveClassifier = ""
    archiveVersion = ""
    from(sourceSets.main.get().output)

    finalizedBy("copyJarToMods")
}

tasks.register<Copy>("copyJarToMods") {
    from(layout.buildDirectory.dir("libs"))
    into(layout.projectDirectory.dir("..").dir("_ModTheSpire").dir("mods"))
}
