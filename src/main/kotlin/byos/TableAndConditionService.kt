package byos

import org.jooq.Condition
import org.jooq.Record
import org.jooq.Table

/**
 *
 */
interface TableAndConditionService {

    /**
     * Returns the table for the given relation with the sql alias.
     */
    fun getTableWithAliasFor(relation: InternalQueryNode.Relation): Table<out Record>

    /**
     * Returns the join condition to be applied between the given two tables for the given relationship.
     */
    fun getConditionFor(relationshipName: String, left: Table<*>, right: Table<*>): Condition?
}
