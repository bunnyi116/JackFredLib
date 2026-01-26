@file:Suppress("UnstableApiUsage", "RedundantNullableReturnType")

import com.github.breadmoirai.githubreleaseplugin.GithubReleaseTask
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.ajoberstar.grgit.Grgit
import red.jackf.GenerateChangelogTask
import red.jackf.UpdateDependenciesTask

plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT" apply false
    id("com.github.breadmoirai.github-release") version "2.4.1" apply false
    id("org.ajoberstar.grgit") version "5.2.1"
    id("maven-publish")
}

///////////////////
// PROJECT UTILS //
///////////////////

// make it nullable because it's able to be null but project.grgit isn't defined to be
val grgit: Grgit? = project.grgit

fun Project.getSourceSet(name: String): SourceSet = this.extensions.getByType(SourceSetContainer::class)[name]

operator fun Any?.unaryPlus() = this!!.toString()

// Adapted from Fabric API; helper for depending on other modules
extra["moduleDependencies"] = fun(project: Project, depNames: List<String>, include: Boolean) {
    val deps = depNames.map {
        project.dependencies.project(path = ":$it")
    }
    val clientOutputs = depNames.map { project(":$it").getSourceSet("client").output }

    project.dependencies {
        deps.forEach {
            add("api", it)
            if (include) add("include", it)
        }

        clientOutputs.forEach {
            add("clientImplementation", it)
        }
    }
}

////////////////
// VERSIONING //
////////////////

var canPublish = grgit != null && System.getenv("RELEASE") != null

fun getVersionSuffix(): String {
    // git branch, or nogit+MCVER
    return grgit?.branch?.current()?.name ?: "nogit+${properties["minecraft_version"]!!}"
}

//println("Prev: " + System.getenv("PREVIOUS_TAG"))
//println("New: " + System.getenv("NEW_TAG"))

if (System.getenv().containsKey("NEW_TAG")) {
    rootProject.version = System.getenv("NEW_TAG").substring(1)
} else {
    val versionStr = "${properties["module_version"]}+${properties["minecraft_version"]!!}"
    canPublish = false
    rootProject.version = if (grgit != null) {
        "$versionStr+dev-${grgit.log()[0].abbreviatedId}"
    } else {
        "$versionStr+dev-nogit"
    }
}

subprojects {
    version = "${+properties["module_version"]}+${getVersionSuffix()}"
}

//println("Can publish v${rootProject.version}: $canPublish")

allprojects {
    group = properties["maven_group"]!!

    apply(plugin = "net.fabricmc.fabric-loom")

    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-Xlint:unchecked")

        options.release.set(21)
    }

    project.extensions.configure<JavaPluginExtension> {
        withSourcesJar()
    }

    project.extensions.configure<LoomGradleExtensionAPI> {
        splitEnvironmentSourceSets()

        runConfigs.configureEach {
            this.isIdeConfigGenerated = false
        }

        mods {
            create(project.name) {
                sourceSet(project.getSourceSet("main"))
                sourceSet(project.getSourceSet("client"))
            }
        }
    }
    /////////////////////////
    // GLOBAL DEPENDENCIES //
    /////////////////////////

    repositories {
        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
            content {
                includeGroup("org.parchmentmc.data")
            }
        }
        mavenCentral()
    }

    dependencies {
        add("minecraft", "com.mojang:minecraft:${properties["minecraft_version"]}")
        /*add("mappings", loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-${properties["parchment_version"]}@zip")
        })*/
        val httpCore = "org.apache.httpcomponents:httpcore:4.4.16"
        add("implementation", httpCore)
        add("implementation", "net.fabricmc:fabric-loader:${properties["loader_version"]}")
        add("implementation", "net.fabricmc.fabric-api:fabric-api:${properties["fabric-api_version"]}")
        add("include", httpCore)

        /*
        // add mixin extras as a depdendency to all, but only bundle on root project
        val mixinExtrasDep = add("implementation",
            add("annotationProcessor",
                "io.github.llamalad7:mixinextras-fabric:${properties["mixin_extras_version"]}")!!)!!

        if (this@allprojects == rootProject)
            add("include", mixinExtrasDep)*/
    }

    ///////////////
    // PACKAGING //
    ///////////////

    val moduleName = project.properties["module_name"]?.toString() ?: "jackfredlib"
    val moduleDescription = project.properties["module_description"]?.toString() ?: "Library for WhereIsIt"
    val rootModuleName = rootProject.properties["module_name"]?.toString() ?: "WhereIsIt"
    val rootModuleDescription = rootProject.properties["module_description"]?.toString() ?: "WhereIsIt project"

    tasks.withType<ProcessResources>().configureEach {
        inputs.property("module_version", project.version)
        inputs.property("module_name", moduleName)
        inputs.property("module_description", moduleDescription)
        inputs.property("root_module_name", rootModuleName)
        inputs.property("root_module_description", rootModuleDescription)

        filesMatching("fabric.mod.json") {
            expand(inputs.properties)
        }
    }


    tasks.named<Jar>("jar") {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${this@allprojects.name}" }
        }
    }
}

////////////////////////
// PACKAGING MAIN JAR //
////////////////////////

subprojects {
    if (name == "jackfredlib-testmod") return@subprojects

    rootProject.tasks.named<Jar>("jar") {
        dependsOn(tasks.named("jar"))
    }

    tasks.named<ProcessResources>("processResources") {
        from(rootProject.file("src/main/resources/assets/jackfredlib/icon.png")) {
            into("assets/jackfredlib")
        }
    }
}

// bundle modules
tasks.named<Jar>("jar") {
    subprojects.forEach {
        if (it.name == "jackfredlib-testmod") return@forEach
        if (it.name == "jackfredlib-config") return@forEach

        dependsOn(it.tasks.named("jar"))
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    doFirst {
        subprojects.forEach { subproj ->
            if (subproj.name == "jackfredlib-testmod") return@forEach
            if (subproj.name == "jackfredlib-config") return@forEach

            val jarTask = subproj.tasks.named<Jar>("jar").get()
            from(zipTree(jarTask.archiveFile.get().asFile))
        }
    }
}

/////////////
// JAVADOC //
/////////////

// Switched to a new Javadoc generation
val aggregateJavadoc by tasks.registering(Javadoc::class) {
    description = "Aggregated Javadoc for all JackFredLib modules"

    subprojects.forEach { proj ->
        if (proj.name == "jackfredlib-testmod") return@forEach

        val main = proj.getSourceSet("main")
        val client = proj.getSourceSet("client")

        source(main.allJava)
        source(client.allJava)
    }

    include("red/jackf/jackfredlib/api/**/*.java")
    include("red/jackf/jackfredlib/client/api/**/*.java")

    val mainCp = getSourceSet("main").compileClasspath
    val clientCp = getSourceSet("client").compileClasspath

    dependsOn(subprojects.mapNotNull { proj ->
        if (proj.name == "jackfredlib-testmod") null
        else proj.tasks.named("jar")
    })

    val subprojectJars = subprojects
        .filter { it.name != "jackfredlib-testmod" }
        .map { proj -> proj.tasks.named<Jar>("jar").flatMap { it.archiveFile } }

    val janksonFiles = project(":jackfredlib-config")
        .configurations
        .getByName("compileClasspath")
        .filter { it.name.contains("jankson", ignoreCase = true) }

    classpath = files(mainCp, clientCp, janksonFiles) + files(subprojectJars)

    (options as StandardJavadocDocletOptions).apply {
        showFromPublic()
        tags(
            "apiNote:a:API Note:",
            "implNote:a:Implementation Note:"
        )
    }
}
// Old JavaDoc generation
/*
tasks.withType<Javadoc>().configureEach {
    options.showFromPublic()

    subprojects.forEach {
        if (it.name == "jackfredlib-testmod") return@forEach

        source(it.getSourceSet("main").allJava)
        source(it.getSourceSet("client").allJava)
    }

    include("red/jackf/jackfredlib/api/**/*.java")
    include("red/jackf/jackfredlib/client/api/**/*.java")

    classpath = files(getSourceSet("main").compileClasspath, getSourceSet("client").compileClasspath)

    subprojects.forEach {
        if (it.name == "jackfredlib-testmod") return@forEach
        classpath += it.getSourceSet("main").compileClasspath
        classpath += it.getSourceSet("client").compileClasspath
    }

    (options as StandardJavadocDocletOptions).tags(
        "apiNote:a:API Note:",
        "implNote:a:Implementation Note:"
    )
}
*/

val javadocJarTask = tasks.register<Jar>("javadocJar") {
    dependsOn(aggregateJavadoc)
    from(aggregateJavadoc.get().destinationDir)
    archiveClassifier = "javadoc"
}

tasks.getByName("build").dependsOn(aggregateJavadoc)

////////////////
// PUBLISHING //
////////////////

fun setupRepositories(repos: RepositoryHandler) {
    // only publish to local on non-CI
    if (!System.getenv().containsKey("CI")) repos.mavenLocal()

    if (canPublish) {
        repos.maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ponuing/JackFredLib")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// Modules
allprojects {
    if (name == "jackfredlib-testmod") return@allprojects

    apply(plugin = "maven-publish")

    val propertiesHandle = properties

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = this@allprojects.name

                from(components["java"])
                if (this@allprojects == rootProject) artifact(javadocJarTask)

                pom {
                    name = +propertiesHandle["module_name"]
                    description = +propertiesHandle["module_description"]
                    url = +propertiesHandle["github_url"]
                    licenses {
                        license {
                            name = "LGPL-3.0"
                            url = "https://opensource.org/license/lgpl-3-0/"
                        }
                    }
                    developers {
                        developer {
                            url = "https://github.com/ponuing"
                            name = "ponuing"
                        }
                    }
                    scm {
                        connection = "scm:git:git://github.com/ponuing/JackFredLib.git"
                        developerConnection = "scm:git:git://github.com/ponuing/JackFredLib.git"
                        url = +propertiesHandle["github_url"]
                    }
                }
            }
        }
        setupRepositories(repositories)
    }
}

// Github Release
if (canPublish) {
    apply(plugin = "com.github.breadmoirai.github-release")

    val lastTagVal = if (System.getenv("PREVIOUS_TAG") == "NONE") null else System.getenv("PREVIOUS_TAG")
    val newTagVal = "v" + rootProject.version

    var generateChangelogTask: TaskProvider<GenerateChangelogTask>? = null

    if (lastTagVal != null) {
        val changelogHeader = if (properties.containsKey("changelogHeaderAddon")) {
            val addonProp = +properties["changelogHeaderAddon"]

            if (addonProp.isNotBlank()) {
                addonProp + "\n\n"
            } else {
                ""
            }
        } else {
            ""
        }

        generateChangelogTask = tasks.register<GenerateChangelogTask>("generateChangelog") {
            lastTag.set(lastTagVal)
            newTag.set(newTagVal)
            githubUrl.set(properties["github_url"]!!.toString())
            prefixFilters.set(properties["changelog_filter"]!!.toString().split(","))

            // Add a bundled block for each module version
            prologue.set(changelogHeader + """
                |## JackFredLib
                |
                |Bundled:
                ${
                    subprojects.filter { it.name != "jackfredlib-testmod" && it.name != "jackfredlib-config" }
                        .joinToString(separator = "\n") { "|  - ${it.properties["module_name"]}: ${it.version}" }
                }
                |
                |## JackFredLib: Config v${project("jackfredlib-config").version}
                |
                |Bundled:
                |  - Jankson: ${properties["jankson_version"]}
                |  - Apache Commons IO: ${properties["commons_io_version"]}
                |  - JackFredLib: Base: ${project("jackfredlib-base").version}
                """.trimMargin())
        }
    }

    tasks.named<GithubReleaseTask>("githubRelease") {
        generateChangelogTask?.let { dependsOn(it) }

        authorization = System.getenv("GITHUB_TOKEN")?.let { "Bearer $it" }
        owner = properties["github_owner"]!!.toString()
        repo = properties["github_repo"]!!.toString()
        tagName = newTagVal
        releaseName = "${properties["mod_name"]} $newTagVal"
        targetCommitish = grgit!!.branch.current().name
        releaseAssets.from(
            tasks["jar"].outputs.files,
            tasks["sourcesJar"].outputs.files,
            tasks["javadocJar"].outputs.files,
        )
        subprojects.forEach {
            if (it.name == "jackfredlib-testmod") return@forEach

            releaseAssets.from(
                it.tasks["jar"].outputs.files,
                it.tasks["sourcesJar"].outputs.files,
            )
        }
        body = if (generateChangelogTask != null) {
            provider {
                generateChangelogTask!!.get().changelogFile.get().asFile.readText()
            }
        } else {
            provider {
                "No Changelog Generated"
            }
        }
    }
}

//////////
// MISC //
//////////

tasks.register<UpdateDependenciesTask>("updateModDependencies") {
    mcVersion.set(properties["minecraft_version"]!!.toString())
    loader.set("fabric")
}