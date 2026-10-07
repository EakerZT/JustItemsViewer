plugins {
    java
    idea
    id("net.neoforged.moddev") version "2.0.144"
}

fun propertyValue(name: String) = providers.gradleProperty(name).get()

group = propertyValue("modGroup")
version = propertyValue("modVersion")

val minecraftVersion = propertyValue("minecraftVersion")
val modId = propertyValue("modId")
val javaVersion = propertyValue("modJavaVersion").toInt()

base {
    archivesName.set("$modId-$minecraftVersion-neoforge")
}

repositories {
    mavenCentral()
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(javaVersion))
    withSourcesJar()
}

val datagen = sourceSets.create("datagen") {
    compileClasspath += sourceSets.main.get().output.classesDirs
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += output + compileClasspath
}

val generatedColors = layout.buildDirectory.dir("generated/resources/guiColors")
val generateGuiColors = tasks.register<JavaExec>("generateGuiColors") {
    dependsOn(tasks.named(datagen.classesTaskName))
    mainClass.set("eakerzt.jiv.common.gui.JivGuiColorsDataGenerator")
    classpath = datagen.runtimeClasspath
    args(generatedColors.get().asFile.absolutePath)
    outputs.dir(generatedColors)
}

sourceSets.main {
    resources.srcDir(generateGuiColors)
}

dependencies {
    implementation("org.jetbrains:annotations:26.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter:${propertyValue("jUnitVersion")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

neoForge {
    version = propertyValue("neoforgeVersion")
    setAccessTransformers("src/main/resources/META-INF/accesstransformer.cfg")
    validateAccessTransformers = true
    addModdingDependenciesTo(sourceSets.test.get())
    mods {
        create(modId) { sourceSet(sourceSets.main.get()) }
    }
    unitTest {
        enable()
        testedMod = mods.named(modId)
    }
    runs {
        create("client") {
            client()
            gameDirectory = file("run/client")
        }
        create("server") {
            server()
            gameDirectory = file("run/server")
            programArguments.add("nogui")
        }
    }
}

val resourceProperties = listOf(
    "modId", "modName", "modAuthor", "modDescription", "minecraftVersionRange",
    "neoforgeVersionRange", "neoforgeLoaderVersionRange"
).associateWith(::propertyValue) + ("version" to version.toString())

tasks.processResources {
    val expansionProperties = resourceProperties
    inputs.properties(resourceProperties)
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(expansionProperties)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(javaVersion)
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks.withType<Jar>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("LICENSE.txt")
    from("NOTICE.txt")
    manifest.attributes(
        "Implementation-Title" to propertyValue("modName"),
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to propertyValue("modAuthor")
    )
}

tasks.test {
    useJUnitPlatform()
}
