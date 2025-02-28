package byos
import org.jooq.codegen.GenerationTool
import java.io.File
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.jooq.meta.jaxb.Configuration
import java.io.InputStream
import kotlin.system.exitProcess

@SpringBootApplication
class ByosApplication

fun main(args: Array<String>) {
    val parameters = parseArgs(args)

    val dbUrl = parameters["dbUrl"] ?: System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/default_db"
    val dbUser = parameters["dbUser"] ?: System.getenv("DB_USER") ?: "postgres"
    val dbPassword = parameters["dbPassword"] ?: System.getenv("DB_PASSWORD") ?: ""
    val schemaPath = parameters["schemaPath"] ?: System.getenv("SCHEMA_PATH") ?: "src/main/resources/graphql/schema.graphqls"
    val targetDir = parameters["targetDir"] ?: "src/generated/java"

    println("Connecting to DB: $dbUrl with user: $dbUser")
    println("Loading GraphQL schema from: $schemaPath")
    println("Using target directory for JOOQ classes: $targetDir")

    generateJooqClasses(dbUrl, dbUser, dbPassword, targetDir)

    runApplication<ByosApplication>(*args)

    exitProcess(0)
}

fun generateJooqClasses(dbUrl: String, dbUser: String, dbPassword: String, targetDir: String) {
    val inputStream: InputStream = object {}.javaClass.getResourceAsStream("/jooq-config.xml")
        ?: throw IllegalArgumentException("JOOQ configuration file not found in resources!")

    val config: Configuration = GenerationTool.load(inputStream)

    config.withJdbc(
        config.jdbc
            .withUrl(dbUrl)
            .withUser(dbUser)
            .withPassword(dbPassword)
    )

    config.generator.target.directory = File(targetDir).absolutePath

    println("Generating JOOQ classes dynamically in: $targetDir")

    GenerationTool().run(config)
}


/**
 * Parse command-line arguments
 */
fun parseArgs(args: Array<String>): Map<String, String> {
    return args.mapNotNull {
        val parts = it.removePrefix("--").split("=")
        if (parts.size == 2) parts[0] to parts[1] else null
    }.toMap()
}
