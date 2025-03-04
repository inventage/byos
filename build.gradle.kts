import nu.studer.gradle.jooq.JooqEdition
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    testImplementation(kotlin("test"))

    implementation("org.postgresql:postgresql:42.2.27")
    implementation("org.jooq:jooq:3.17.6")
    jooqGenerator("org.postgresql:postgresql:42.2.27")

    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework:spring-webflux")
    testImplementation("org.springframework.graphql:spring-graphql-test")
}

//https://docs.gradle.org/current/userguide/publishing_maven.html


sourceSets {
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
            srcDir("src/test/java-generated")
            srcDir("src/test/java")
        }
        kotlin {
            srcDir("src/test/kotlin")
        }
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
tasks.bootJar {
    enabled = false
}

tasks.test {
    useJUnitPlatform()
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
// TODO: Task only when...
//jooq {
//    version.set("3.17.6")
//    edition.set(JooqEdition.OSS)
//
//    configurations {
//        create("main") {
//            jooqConfiguration.apply {
//                logging = org.jooq.meta.jaxb.Logging.DEBUG
//
//                jdbc.apply {
//                    driver = "org.postgresql.Driver"
//                    url = "jdbc:postgresql://localhost:5432/sakila"
//                    user = "postgres"
//                    password = ""
//                }
//                generator.apply {
//                    name = "org.jooq.codegen.JavaGenerator"
//                    database.apply {
//                        name = "org.jooq.meta.postgres.PostgresDatabase"
//                        inputSchema = "public"
//                        includes = ".*"
//                        excludes = ""
//                    }
//                    target.apply {
//                        packageName = "db.jooq.generated"
//                        directory = "${project.projectDir}/src/test/java-generated"
//                    }
//                }
//            }
//        }
//    }
//}
