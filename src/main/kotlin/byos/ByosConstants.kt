package byos

object ByosConstants {

    /**
     * https://spec.graphql.org/June2018/#sec-Type-Name-Introspection
     */
    const val GRAPHQL_TYPENAME_INTROSPECTION = "__typename"

    /**
     * https://spec.graphql.org/June2018/#sec-Language.Operations
     */
    const val GRAPHQL_OPERATION_QUERY = "query";

    /**
     * https://spec.graphql.org/June2018/#sec-Operation-Name-Uniqueness
     */
    const val GRAPHQL_OPERATION_NAME = "operationName";

    /**
     * https://spec.graphql.org/June2018/#sec-Validation.Variables
     */
    const val GRAPHQL_VARIABLES = "variables";

    /**
     * https://spec.graphql.org/June2018/#sec-Data
     */
    const val GRAPHQL_RESULT_DATA = "data";

    /**
     * https://spec.graphql.org/June2018/#sec-Errors
     */
    const val GRAPHQL_RESULT_ERRORS = "errors";

    /**
     * Argument name for specifying the number of rows to retain from the result set.
     */
    const val LIMIT = "limit";

    /**
     * Argument name for specifying the cursor position.
     */
    const val AFTER = "after";

    /**
     * Argument name for specifying restriction clauses.
     */
    const val WHERE = "where";

    /**
     * Argument name for specifying the result ordering.
     */
    const val ORDER_BY = "orderBy";

    /**
     * Argument name to return only distinct (different) values.
     */
    const val DISTINCT_ON = "distinctOn"


    /**
     * Argument name for specifying which slice to retain from the results.
     */
    const val CURSOR = "cursor";

    /**
     * Option name for specifying ascending order
     */
    const val ORDER_BY_ASCENDING = "asc";

    /**
     * Option name for specifying descending order
     */
    const val ORDER_BY_DESCENDING = "desc";

    /**
     * Concatenation operator for and
     */
    const val CONDITION_AND = "_and";

    /**
     * Concatenation operator for not
     */
    const val CONDITION_NOT = "_not";

    /**
     * Concatenation operator for or
     */
    const val CONDITION_OR = "_or";

    /**
     * Comparison operator for equals
     */
    const val COMPARISON_EQ = "_eq";

    /**
     * Comparison operator for not equals
     */
    const val COMPARISON_NEQ = "_neq";

    /**
     * Comparison operator for lesser
     */
    const val COMPARISON_LT = "_lt";

    /**
     * Comparison operator for lesser or equals
     */
    const val COMPARISON_LTE = "_lte";

    /**
     * Comparison operator for greater
     */
    const val COMPARISON_GT = "_gt";

    /**
     * Comparison operator for greater or equals
     */
    const val COMPARISON_GTE = "_gte";

    /**
     * Comparison operator for like
     */
    const val COMPARISON_LIKE = "_like";

    /**
     * Comparison operator for like ignoring case
     */
    const val COMPARISON_ILLIKE = "_illike";

    /**
     * Comparison operator for regex
     */
    const val COMPARISON_REGEX = "_regex";

    /**
     * Comparison operator for regex ignoring case
     */
    const val COMPARISON_IREGEX = "_iregex";

    /**
     * Comparison operator for in
     */
    const val COMPARISON_IN = "_in";

    /**
     * Comparison operator for not in
     */
    const val COMPARISON_NIN = "_nin";

    /**
     * Comparison operator for is null
     */
    const val COMPARISON_IS_NULL = "_is_null";


}