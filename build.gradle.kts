import com.badlogic.gdx.tools.texturepacker.TexturePacker
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.apache.tools.ant.filters.ReplaceTokens
import java.util.jar.JarFile

plugins {
    `java-library`
    kotlin("jvm") version "1.7.22"
}

val modID = "haberdashery"
group = "com.evacipated.cardcrawl.mod"
version = "0.1"
description = "Haberdashery"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val steamPath = project.properties["sts.steamPath"] ?: "C:/Program Files (x86)/Steam/steamapps"
val localLibPath = project.properties["sts.localLibPath"]
val modsPath = project.properties["sts.modsPath"] ?: "$steamPath/common/SlayTheSpire/mods"

extra.apply {
    val srcAssets = project.rootDir.resolve("srcAssets")
    mapOf(
        "id" to modID,
        "name" to description,
        "version" to version,
        "sts_version" to "12-22-2020",
        "mts_version" to "3.21.0",
        "authors" to srcAssets.resolve("authors.txt").readLines()
            .joinToString(separator = "\", \""),
        "credits" to srcAssets.resolve("credits.txt").readText().trimIndent(),
        "description" to srcAssets.resolve("description.txt").readText().trimIndent(),
        "dependencies" to listOf("basemod")
            .joinToString(separator = "\", \""),
    ).forEach { (k, v) -> set(k, v) }
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("com.google.code.gson:gson:2.11.0")
        classpath("com.badlogicgames.gdx:gdx-tools:1.9.5")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(sts())
    implementation(sts("modthespire"))
    implementation(sts("basemod"))
}

tasks.processResources {
    dependsOn("texturePacker")

    filteringCharset = "UTF-8"
    val expansion: FileCopyDetails.() -> Unit = {
        val tokens = project.extra.properties.entries
            .filter { it.value is String }
            .associate { "mod.${it.key}" to it.value }
        filter(
            ReplaceTokens::class,
            "tokens" to tokens,
            "beginToken" to "\${",
            "endToken" to "}",
        )
    }
    filesMatching("ModTheSpire.json", expansion)
    filesMatching("${modID}Assets/**/*.json", expansion)
    filesMatching("${modID}/audio/**/*.json", expansion)
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
    into(modsPath)
}

tasks.register("texturePacker") {
    val packs = listOf(
        "vfx",
    ).map {
        val input = project.rootDir.resolve("srcAssets").resolve(it)
        val output = project.layout.buildDirectory.dir("generated/resources/${modID}Assets/images/$it")
        inputs.dir(input)
        outputs.dir(output)
        Triple(input.path, output.get().asFile.path, it)
    }
    doLast {
        packs.forEach {
            TexturePacker.process(it.first, it.second, it.third)
        }
    }
}

sourceSets {
    getByName("main") {
        output.dir(project.layout.buildDirectory.dir("generated/resources"))
    }
}

// =============================================
// Helper functions for finding mod dependencies
// =============================================
fun DependencyHandler.sts(modID: String? = null) = run {
    if (modID == null) {
        files("$steamPath/common/SlayTheSpire/desktop-1.0.jar")
    } else {
        val gson = GsonBuilder().create()
        val libTree = localLibPath?.let { fileTree(it).apply {
            include("*.jar")
        } }
        val steamTree = fileTree("$steamPath/workshop/content/646570").apply {
            include("*/*.jar")
        }
        val tree = if (libTree != null) {
            libTree + steamTree
        } else {
            steamTree
        }

        val files = tree.filter { file ->
            val jar = JarFile(file)
            val mtsJson = jar.getEntry("ModTheSpire.json")
            if (mtsJson != null) {
                val map = gson.fromJson<Map<String, Any?>>(
                    jar.getInputStream(mtsJson).reader(),
                    object : TypeToken<Map<String, Any?>>() {}.type
                )
                map["modid"] == modID
            } else {
                false
            }
        }
        files(files.first())
    }
}

fun DependencyHandler.sts(steamID: Int) = run {
    fileTree("$steamPath/workshop/content/646570/$steamID").apply {
        include("*.jar")
    }
}
