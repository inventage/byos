package byos

object ByosConstants {

    /**
     * http://spec.graphql.org/June2018/#sec-Type-Name-Introspection
     */
    const val GRAPHQL_TYPENAME_INTROSPECTION = "__typename"

    /**
     * Argument name for specifying the number of rows to retain from the result set.
     */
    const val LIMIT = "limit";

    const val WHERE = "where";

    /**
     * Argument name for specifying which slice to retain from the results.
     */
    const val CURSOR = "cursor";

    const val ORDER_BY = "orderBy";

    const val ORDER_BY_ASCENDING = "asc";

    const val ORDER_BY_DESCENDING = "desc";

}