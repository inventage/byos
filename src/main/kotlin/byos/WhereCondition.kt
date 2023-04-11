package byos

import db.jooq.generated.Tables.AUTHORS
import db.jooq.generated.Tables.BOOKS
import org.jooq.Condition
import org.jooq.impl.DSL

object WhereCondition {
    private val allConditions = mapOf(
        StringPair(BOOKS.name, AUTHORS.name) to DSL.condition(BOOKS.AUTHORID.eq(AUTHORS.ID)),
    )

    fun getFor(table1: String, table2: String): Condition {
        return allConditions[StringPair(table1, table2)]
            ?: error("No condition found for tables $table1 and $table2")
    }
}

data class StringPair(val first: String, val second: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StringPair) return false

        val set1 = setOf(this.first, this.second)
        val set2 = setOf(other.first, other.second)

        return set1 == set2
    }

    override fun hashCode(): Int {
        return setOf(this.first, this.second).hashCode()
    }
}
