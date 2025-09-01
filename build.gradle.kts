import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import nu.studer.gradle.jooq.JooqEdition
import java.nio.file.Files
import java.nio.file.Paths

plugins {
    kotlin("jvm") version "1.8.0"
    application
    id("nu.studer.jooq") version "8.1"
    id("org.springframework.boot") version "3.0.4"
    id("io.spring.dependency-management") version "1.1.0"
    kotlin("plugin.spring") version "1.7.22"
    `maven-publish`
}

java.sourceCompatibility = JavaVersion.VERSION_17

val projectVersion: String by lazy {
    project.findProperty("version")?.toString() ?: "1.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    jooqGenerator("org.postgresql:postgresql:42.2.27")

    implementation("org.postgresql:postgresql:42.2.27")
    implementation("org.jooq:jooq:3.17.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.graphql-java:graphql-java:21.5")

    testImplementation(kotlin("test"))
    testImplementation("org.springframework.boot:spring-boot-starter-graphql")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework:spring-webflux")
    testImplementation("org.springframework.graphql:spring-graphql-test")
}

//https://docs.gradle.org/current/userguide/publishing_maven.html

sourceSets {
    create("integrationTest") {
        java.srcDir("src/integration-test/java")
        java.srcDir("src/integration-test/java-generated")
        kotlin.srcDir("src/integration-test/kotlin")
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }

    main {
        java {
            srcDir("src/main/java")
        }
        kotlin {
            srcDir("src/main/kotlin")
        }
    }
    test {
        java {
            srcDir("src/test/java")
        }
        kotlin {
            srcDir("src/test/kotlin")
        }
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    useJUnitPlatform()

    shouldRunAfter(tasks.test)
}

// produces byos-sources.jar in build/libs
tasks {
    val sourcesJar by creating(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").java.srcDirs)
        from(sourceSets.getByName("main").kotlin.srcDirs)
    }

    artifacts {
        archives(sourcesJar)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.inventage.graphql"
            artifactId = "byos"
            version = projectVersion
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "Nexus"
            url = uri("https://nexus3.inventage.com/repository/inventage-projectware-maven-staging/")
            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }
        }
    }
}

tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
    exclude("db/**")
}

tasks.bootJar {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
jooq {
    version.set("3.17.6")
    edition.set(JooqEdition.OSS)

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.DEBUG

                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/sakila"
                    user = "postgres"
                    password = "postgres"
                }
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        includes = ".*"
                        excludes = ""
                    }
                    target.apply {
                        packageName = "db.jooq.generated"
                        directory = "${project.projectDir}/src/integration-test/java-generated"
                    }
                }
            }
        }
    }
}


val jooqOutputDir = File("${project.projectDir}/src/integration-test/java-generated")

tasks.register("startPostgresContainer") {
    doLast {
        val processCheck = ProcessBuilder("bash", "-c", "docker ps --format '{{.Ports}}' | grep '5432->'")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val runningContainers = processCheck.inputStream.bufferedReader().readText().trim()

        val dirPath = Paths.get("${project.projectDir}/src/integration-test/java-generated")
        val dirExists = Files.exists(dirPath)

        if (runningContainers.isNotEmpty()) {
            println("A container is already running on port 5432. Skipping task.")
            return@doLast
        }

        if (dirExists) {
            println("Directory '/src/integration-test/java-generated' already exists. Skipping task.")
            return@doLast
        }

        println("Running setup_db.sh script...")
        val processSetup = ProcessBuilder("bash", "${project.projectDir}/setup_db.sh")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val exitCodeSetup = processSetup.waitFor()
        if (exitCodeSetup != 0) {
            throw RuntimeException("setup_db.sh failed with exit code $exitCodeSetup")
        }

        println("PostgreSQL setup complete!")
    }
}


tasks.register("stopPostgresContainer") {
    doLast {
        println("Stopping PostgreSQL container...")
        val process = ProcessBuilder("docker", "stop", "sakila")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            println("Failed to stop PostgreSQL container. It may not have been running.")
        } else {
            println("PostgreSQL container stopped.")
        }
    }
}


tasks.named("generateJooq") {
    onlyIf {
        val exists = jooqOutputDir.exists() && jooqOutputDir.list()?.isNotEmpty() == true
        if (exists) {
            println("JOOQ classes already exist, skipping generation.")
        }
        !exists
    }
    dependsOn("startPostgresContainer")
    finalizedBy("stopPostgresContainer")
}

tasks.register("endPostgresContainer") {
    doLast {
        val process = ProcessBuilder("bash", "${project.projectDir}/setup_db.sh")
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            throw RuntimeException("setup_db.sh failed with exit code $exitCode")
        }
        println("PostgreSQL setup complete!")
    }
}


tasks.named("build") {
    dependsOn("generateJooq")
}

