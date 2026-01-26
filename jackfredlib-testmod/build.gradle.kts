@file:Suppress("UnstableApiUsage")

import java.net.URI

base {
    archivesName.set("${properties["archives_base_name"]}-testmod")
}

loom {
    log4jConfigs.from(rootProject.file("log4j2.xml"))

    runConfigs.forEach {
        it.isIdeConfigGenerated = false
    }

    runConfigs.register("testClient") {
        client()
        ideConfigGenerated(true)
        name("Test Mod Client")

        programArgs.addAll("--username JackFred".split(" "))
    }
}

repositories {
    maven {
        name = "TerraformersMC"
        url = URI("https://maven.terraformersmc.com/releases/")
        content {
            includeGroup("com.terraformersmc")
            includeGroup("dev.emi")
        }
    }
    maven {
        name = "Modrinth"
        url = URI("https://api.modrinth.com/maven")
        content {
            includeGroup("maven.modrinth")
        }
    }

    mavenCentral()
}

@Suppress("UNCHECKED_CAST")
val moduleDependencies = rootProject.extra["moduleDependencies"] as (Project, List<String>, Boolean) -> Unit

moduleDependencies(
    project,
    listOf(
        "jackfredlib-base",
        "jackfredlib-colour",
        "jackfredlib-extracommandsourcedata",
        "jackfredlib-gps",
        "jackfredlib-lying",
        "jackfredlib-toasts",
        "jackfredlib-config"
    ),
    false
)

dependencies {
    implementation(rootProject)

    implementation("blue.endless:jankson:${properties["jankson_version"]}")
    implementation("commons-io:commons-io:${properties["commons_io_version"]}")

    // modRuntimeOnly("com.terraformersmc:modmenu:${properties["modmenu_version"]}")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
}

tasks.test {
    useJUnitPlatform()
}