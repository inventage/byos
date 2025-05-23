package byos

import byos.ByosConstants.AFTER
import byos.ByosConstants.ORDER_BY
import byos.ByosConstants.WHERE
import byos.ByosConstants.DISTINCT_ON
import com.fasterxml.jackson.databind.JsonNode
import graphql.language.*
import graphql.schema.GraphQLSchema
import org.jooq.Condition
import org.jooq.JSON
import org.jooq.impl.DSL
import java.math.BigInteger

sealed class InternalQueryNode(val graphQLFieldName: String, val graphQLAlias: String) {
    class Relation(
        graphQLFieldName: String,
        graphQLAlias: String,
        val sqlAlias: String,
        val fieldTypeInfo: FieldTypeInfo,
        val children: List<InternalQueryNode>,
        val arguments: List<Argument>,
        val connectionInfo: ConnectionInfo?,
    ) : InternalQueryNode(graphQLFieldName, graphQLAlias)

    class Attribute(graphQLFieldName: String, graphQLAlias: String) : InternalQueryNode(graphQLFieldName, graphQLAlias)
}

data class FieldTypeInfo(val graphQLTypeName: String, val isList: Boolean) {
    val relationName = graphQLTypeName//.lowercase()
}

data class ConnectionInfo(
    // currently only supports one edge and node per connection
    val edgesGraphQLAlias: String,
    val nodeGraphQLAlias: String,
    val cursorGraphQLAliases: List<String>,
    val totalCountGraphQLAliases: List<String>,
    val pageInfos: List<PageInfo>
)

data class PageInfo(
    val graphQLAlias: String,
    val hasNextPageGraphQlAliases: List<String>,
    val endCursorGraphQlAliases: List<String>,
)

class QueryTranspiler(
    private val whereCondition: WhereCondition,
    private val schema: GraphQLSchema,
    private val tableAndConditionService: TableAndConditionService
) {

    fun buildInternalQueryTrees(
        queryDefinition: OperationDefinition,
        fragments: List<FragmentDefinition>
    ): List<InternalQueryNode.Relation> =
        getChildrenFromSelectionSet(queryDefinition.selectionSet, fragments, Counter()).map {
            it as InternalQueryNode.Relation
        }
    private fun collectFieldsFromSelectionSet(
        selectionSet: SelectionSet,
        fragments: List<FragmentDefinition>
    ): List<Field> {
        val fields = mutableListOf<Field>()

        for (selection in selectionSet.selections) {
            when (selection) {
                is Field -> fields += selection

                is FragmentSpread -> {
                    val fragment = fragments.single { it.name == selection.name }
                    fields += collectFieldsFromSelectionSet(fragment.selectionSet, fragments)
                }

                is InlineFragment -> {
                    fields += collectFieldsFromSelectionSet(selection.selectionSet, fragments)
                }
            }
        }
        return fields
    }

    private fun getChildrenFromSelectionSet(
        selectionSet: SelectionSet,
        fragments: List<FragmentDefinition>,
        counter: Counter,
        parentGraphQlTypeName: String = schema.queryType.name
    ): List<InternalQueryNode> {
        val fieldsFromSpread = collectFieldsFromSelectionSet(selectionSet, fragments)
        val result = fieldsFromSpread.map { selection ->
            val subSelectionSet = selection.selectionSet
            when {
                subSelectionSet == null -> {
                    InternalQueryNode.Attribute(
                        graphQLFieldName = selection.name,
                        graphQLAlias = selection.alias ?: selection.name
                    )
                }

                subSelectionSet.selections.any { it is Field && it.name == "edges" } -> {
                    val edgesSelection =
                        subSelectionSet.selections.filterIsInstance<Field>().single {
                            it.name == "edges"
                        }
                    val nodeSelection =
                        edgesSelection.selectionSet!!.selections.filterIsInstance<Field>()
                            .single { it.name == "node" }
                    val nodeSubSelectionSet = nodeSelection.selectionSet
                    val pageInfoSelections =
                        subSelectionSet.selections.filterIsInstance<Field>().filter {
                            it.name == "pageInfo"
                        }

                    val queryTypeInfo =
                        getFieldTypeInfo(schema, selection.name, parentGraphQlTypeName)
                    val edgesTypeInfo =
                        getFieldTypeInfo(
                            schema,
                            edgesSelection.name,
                            queryTypeInfo.graphQLTypeName
                        )
                    val nodeTypeInfo =
                        getFieldTypeInfo(
                            schema,
                            nodeSelection.name,
                            edgesTypeInfo.graphQLTypeName
                        )

                    InternalQueryNode.Relation(
                        graphQLFieldName = selection.name,
                        graphQLAlias = selection.alias ?: selection.name,
                        sqlAlias = "${selection.name}-${counter.getIncrementingNumber()}",
                        fieldTypeInfo = nodeTypeInfo,
                        children =
                        getChildrenFromSelectionSet(
                            nodeSubSelectionSet,
                            fragments,
                            counter,
                            nodeTypeInfo.graphQLTypeName
                        ),
                        arguments = selection.arguments,
                        connectionInfo =
                        ConnectionInfo(
                            edgesGraphQLAlias = edgesSelection.alias
                                ?: edgesSelection.name,
                            nodeGraphQLAlias = nodeSelection.alias
                                ?: nodeSelection.name,
                            cursorGraphQLAliases =
                            edgesSelection.selectionSet!!
                                .selections
                                .filterIsInstance<Field>()
                                .filter { it.name == ByosConstants.CURSOR }
                                .map { it.alias ?: it.name },
                            totalCountGraphQLAliases =
                            subSelectionSet
                                .selections
                                .filterIsInstance<Field>()
                                .filter { it.name == "totalCount" }
                                .map { it.alias ?: it.name },
                            pageInfos =
                            pageInfoSelections.map { pageInfoSelection
                                ->
                                val pageInfoSelectionSet =
                                    pageInfoSelection.selectionSet
                                PageInfo(
                                    graphQLAlias =
                                    pageInfoSelection.alias
                                        ?: pageInfoSelection
                                            .name,
                                    hasNextPageGraphQlAliases =
                                    pageInfoSelectionSet!!
                                        .selections
                                        .filterIsInstance<
                                                Field>()
                                        .filter {
                                            it.name ==
                                                    "hasNextPage"
                                        }
                                        .map {
                                            it.alias
                                                ?: it.name
                                        },
                                    endCursorGraphQlAliases =
                                    pageInfoSelectionSet
                                        .selections
                                        .filterIsInstance<
                                                Field>()
                                        .filter {
                                            it.name ==
                                                    "endCursor"
                                        }
                                        .map {
                                            it.alias
                                                ?: it.name
                                        },
                                )
                            }
                        )
                    )
                }

                else -> {
                    val fieldTypeInfo =
                        getFieldTypeInfo(schema, selection.name, parentGraphQlTypeName)
                    InternalQueryNode.Relation(
                        graphQLFieldName = selection.name,
                        graphQLAlias = selection.alias ?: selection.name,
                        sqlAlias = "${selection.name}-${counter.getIncrementingNumber()}",
                        fieldTypeInfo = fieldTypeInfo,
                        children =
                        getChildrenFromSelectionSet(
                            subSelectionSet,
                            fragments,
                            counter,
                            fieldTypeInfo.graphQLTypeName
                        ),
                        arguments = selection.arguments,
                        connectionInfo = null
                    )
                }
            }
        }
        return result
    }

//    fun resolveInternalQueryTree(
//        relation: InternalQueryNode.Relation,
//        joinCondition: Condition = DSL.noCondition()
//    ): org.jooq.Field<JSON> {
//        return resolveInternalQueryTree(relation, Collections.emptyMap(), joinCondition)
//    }

    fun resolveInternalQueryTree(
        relation: InternalQueryNode.Relation,
        variables: Map<String, JsonNode>,
        joinCondition: Condition = DSL.noCondition()
    ): org.jooq.Field<JSON> {
        val outerTable = tableAndConditionService.getTableWithAliasFor(relation)

        val (relations, attributes) =
            relation.children.partition { it is InternalQueryNode.Relation }
        val attributeNames =
            attributes.distinctBy { it.graphQLAlias }.map { attribute ->
                if (ByosConstants.GRAPHQL_TYPENAME_INTROSPECTION.equals(attribute.graphQLFieldName)) {
                    DSL.inline(relation.fieldTypeInfo.graphQLTypeName).`as`(ByosConstants.GRAPHQL_TYPENAME_INTROSPECTION)
                }
                else {
                    outerTable
                        .field(attribute.graphQLFieldName)//.lowercase())
                        ?.`as`(attribute.graphQLAlias)
                        ?: error(
                            "Field ${attribute.graphQLFieldName} does not exist on table $outerTable"
                        )
                }
            }

        val subSelects =
            relations.map { subRelation ->
                val innerTable =
                    tableAndConditionService.getTableWithAliasFor(
                        subRelation as InternalQueryNode.Relation
                    )
                resolveInternalQueryTree(
                    subRelation,
                    variables,
                    whereCondition.getForRelationship(
                        subRelation.graphQLFieldName,
                        outerTable,
                        innerTable
                    )
                )
            }

        val (paginationArgument, otherArguments) =
            relation.arguments.partition { it.name == ByosConstants.LIMIT }
        val (orderByArgument, otherArguments2) = otherArguments.partition { it.name == ORDER_BY }
        val (afterArgument, otherArguments3) = otherArguments2.partition { it.name == AFTER }
        val (whereArgument, otherArguments4) = otherArguments3.partition { it.name == WHERE }
        val (distinctOnArgument, filterArguments) = otherArguments4.partition { it.name == DISTINCT_ON }

        val distinctOnFields = (distinctOnArgument.firstOrNull()?.value as? ArrayValue)
            ?.values
            ?.mapNotNull { (it as? EnumValue)?.name }
            ?.mapNotNull { outerTable.field(it) }
            .orEmpty()


        //val limitValue = (paginationArgument.firstOrNull()?.value as IntValue?)?.value
        val limitValue = ConditionFactory.extractIntValue(paginationArgument.firstOrNull()?.value, variables)?.value

        val providedOrderCriteria: Map<org.jooq.Field<*>, String> =
            (orderByArgument.firstOrNull()?.value as? ArrayValue)
                ?.values
                ?.filterIsInstance<ObjectValue>()
                ?.flatMap { obj ->
                    obj.objectFields.mapNotNull { field ->
                        val column = outerTable.field(field.name.lowercase())
                        val direction = (field.value as? EnumValue)?.name
                        if (column != null && direction != null) column to direction else null
                    }
                }
                ?.toMap(LinkedHashMap())
                .orEmpty()
        val primaryKeyFields = outerTable.primaryKey?.fields?.map { outerTable.field(it)!! } ?: outerTable.fields().toList()

        val orderByFields = providedOrderCriteria.keys +
                (distinctOnFields - providedOrderCriteria.keys).toSet() +
                (primaryKeyFields - providedOrderCriteria.keys - distinctOnFields.toSet()).toSet()

        val orderByFieldsWithDirection = orderByFields
            .map {
                when (providedOrderCriteria[it]) {
                    ByosConstants.ORDER_BY_DESCENDING -> it.desc()
                    else -> it.asc()
                }
            }
        val cursor = DSL.jsonObject(*orderByFields.toTypedArray()).cast(String::class.java).`as`(ByosConstants.CURSOR)

        val afterCondition = afterArgument
            .firstOrNull()
            ?.let { whereCondition.getForAfterArgument(it, orderByFieldsWithDirection, outerTable) }
            ?: DSL.noCondition()

        val where = whereArgument.map { whereCondition.getForWhere(it, variables, outerTable)}


        val cursorRequested = relation.connectionInfo?.cursorGraphQLAliases?.isNotEmpty() ?: false
        val endCursorRequested = relation.connectionInfo?.pageInfos?.flatMap { it.endCursorGraphQlAliases }?.isNotEmpty() ?: false
        val hasNextPageRequested = relation.connectionInfo?.pageInfos?.flatMap { it.hasNextPageGraphQlAliases }?.isNotEmpty() ?: false

        val cteSelect = DSL.select()
            .apply {
                if (distinctOnFields.isNotEmpty()) {
                    distinctOn(*distinctOnFields.toTypedArray())
                }
            }
        val cte =
            DSL.name("cte")
                .`as`(
                    cteSelect.select(attributeNames)
                        .select(subSelects)
                        .apply {
                            if (cursorRequested || endCursorRequested)
                                select(cursor)
                        }
                        .apply {
                            if (hasNextPageRequested)
                                select(
                                    DSL.count()
                                        .over()
                                        .`as`("count_after_cursor")
                                )
                        }
                        .from(outerTable)
                        .where(where)
                        .and(joinCondition)
                        .and(afterCondition)
                        .orderBy(orderByFieldsWithDirection)
                        .apply { if (limitValue != null) limit(limitValue) }
                )

        val totalCountSubquery =
            DSL.selectCount().from(outerTable).where(where).and(joinCondition)

        val endCursorSubquery =
            DSL.select(DSL.lastValue(DSL.field(cursor.name)).over())
                .from(cte)
                .limit(1)

        return DSL.field(
            DSL.with(cte)
                .select(
                    when {
                        relation.connectionInfo != null -> {
                            DSL.jsonObject(
                                DSL.key(
                                    relation.connectionInfo
                                        .edgesGraphQLAlias
                                )
                                    .value(
                                        DSL.coalesce(
                                            DSL.jsonArrayAgg(
                                                DSL.jsonObject(
                                                    DSL.key(
                                                        relation.connectionInfo
                                                            .nodeGraphQLAlias
                                                    )
                                                        .value(
                                                            DSL.jsonObject(
                                                                *attributeNames
                                                                    .toTypedArray(),
                                                                *subSelects
                                                                    .toTypedArray()
                                                            )
                                                        ),
                                                    *relation.connectionInfo
                                                        .cursorGraphQLAliases
                                                        .map {
                                                            DSL.key(
                                                                it
                                                            )
                                                                .value(
                                                                    DSL.field(
                                                                        cursor.name
                                                                    )
                                                                )
                                                        }
                                                        .toTypedArray()
                                                )
                                            ),
                                            DSL.jsonArray()
                                        )
                                    ),
                                *relation.connectionInfo
                                    .totalCountGraphQLAliases
                                    .map {
                                        DSL.key(it)
                                            .value(
                                                totalCountSubquery
                                            )
                                    }
                                    .toTypedArray(),
                                *relation.connectionInfo
                                    .pageInfos
                                    .map { pageInfo ->
                                        DSL.key(pageInfo.graphQLAlias)
                                            .value(
                                                DSL.jsonObject(
                                                    *pageInfo.hasNextPageGraphQlAliases
                                                        .map {
                                                            DSL.key(
                                                                it
                                                            )
                                                                .value(
                                                                    when (limitValue
                                                                    ) {
                                                                        null ->
                                                                            false
                                                                        BigInteger
                                                                            .ZERO ->
                                                                            true
                                                                        else ->
                                                                            DSL.max(
                                                                                DSL.field(
                                                                                    "count_after_cursor"
                                                                                )
                                                                            )
                                                                                .greaterThan(
                                                                                    limitValue
                                                                                )
                                                                    }
                                                                )
                                                        }
                                                        .toTypedArray(),
                                                    *pageInfo.endCursorGraphQlAliases
                                                        .map {
                                                            DSL.key(
                                                                it
                                                            )
                                                                .value(
                                                                    endCursorSubquery
                                                                )
                                                        }
                                                        .toTypedArray()
                                                )
                                            )
                                    }
                                    .toTypedArray()
                            )
                        }
                        relation.fieldTypeInfo.isList -> {
                            DSL.coalesce(
                                DSL.jsonArrayAgg(
                                    DSL.jsonObject(
                                        *attributeNames
                                            .toTypedArray(),
                                        *subSelects.toTypedArray()
                                    )
                                ),
                                DSL.jsonArray()
                            )
                        }
                        else -> {
                            DSL.jsonObject(
                                *attributeNames.toTypedArray(),
                                *subSelects.toTypedArray()
                            )
                        }
                    }
                )
                .from(cte)
        )
            .`as`(relation.graphQLAlias)
    }
}

