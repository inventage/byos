package example

import byos.QueryTranspiler
import byos.WhereCondition
import byos.executeJooqQuery
import byos.formatGraphQLResponse
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.GraphQL
import graphql.introspection.IntrospectionQuery
import graphql.language.Document
import graphql.language.FragmentDefinition
import graphql.language.OperationDefinition
import graphql.parser.Parser
import graphql.schema.idl.RuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.validation.Validator
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.io.File
import java.util.Locale
import java.util.stream.Collectors

@Component
class GraphQLService {
    companion object {
        private val schemaFile = File("src/integration-test/resources/graphql/schema.graphqls")
        private val schema = SchemaGenerator().makeExecutableSchema(SchemaParser().parse(schemaFile), RuntimeWiring.newRuntimeWiring().build())
        private val graphQL = GraphQL.newGraphQL(schema).build()
        private val objectMapper = ObjectMapper()
        private val parser = Parser()
        private val tableAndConditionService = HardcodedTableAndConditionService()
        private val queryTranspiler = QueryTranspiler(WhereCondition(tableAndConditionService), schema, tableAndConditionService)
    }

    fun executeGraphQLQuery(requestInfo: RequestInfo): String {
        val (document, selectedQuery, _) = requestInfo

        val errors = Validator().validateDocument(schema, document, Locale.ENGLISH)
        if (errors.isNotEmpty()) {
            return objectMapper.writeValueAsString(
                mapOf("data" to null, "errors" to errors.map { it.toSpecification() })
            )
        }

        val ast = selectedQuery?.let { document.getOperationDefinition(it).get() }
            ?: document.definitions.filterIsInstance<OperationDefinition>().single()

        if (ast.name == "IntrospectionQuery") {
            val result = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY)
            return objectMapper.writeValueAsString(result.toSpecification())
        }
        val fragments = document.definitions.filterIsInstance<FragmentDefinition>()

        val queryTrees = queryTranspiler.buildInternalQueryTrees(ast, fragments)
        val results =
            queryTrees.map { tree ->
                executeJooqQuery { ctx ->
                    ctx.select(queryTranspiler.resolveInternalQueryTree(tree, requestInfo.variables)).fetch()
                }
            }
        results.map(::println)
        return results.formatGraphQLResponse()
    }

    fun executeGraphQLQuery(query: String) = executeGraphQLQuery(RequestInfo(parser.parseDocument(query), null, emptyMap()))

    fun extractRequestInfo(request: HttpServletRequest): RequestInfo? {
        val requestBody = request.reader.lines().collect(Collectors.joining())
        val jsonNode = objectMapper.readTree(requestBody)
        val queries = jsonNode["query"]?.textValue()
        if (queries.isNullOrBlank()) return null
        val document = parser.parseDocument(queries)
        val variableDefinitions = jsonNode["variables"]?.fields()?.asSequence()?.associate { (key, value) -> key to value } ?: emptyMap()
        val selectedQuery = jsonNode["operationName"]?.textValue()
        return RequestInfo(document, selectedQuery, variableDefinitions)
    }
}

data class RequestInfo(val document: Document, val selectedQuery: String?, val variables: Map<String, JsonNode>)
