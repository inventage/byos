package example

import db.jooq.generated.Tables
import db.jooq.generated.tables.Actor
import db.jooq.generated.tables.Category
import db.jooq.generated.tables.Film
import db.jooq.generated.tables.Inventory
import db.jooq.generated.tables.Language
import db.jooq.generated.tables.Store
import org.jooq.Condition
import org.jooq.Table
import org.jooq.impl.DSL

fun getConditionForRelationship(relationshipName: String, left: Table<*>, right: Table<*>): Condition? =
    when {
        relationshipName == "actors" && left is Film && right is Actor -> DSL.exists(
            DSL.selectOne().from(Tables.FILM_ACTOR).where(left.FILM_ID.eq(Tables.FILM_ACTOR.FILM_ID).and(Tables.FILM_ACTOR.ACTOR_ID.eq(right.ACTOR_ID)))
        )

        relationshipName == "films" && left is Actor && right is Film -> DSL.exists(
            DSL.selectOne().from(Tables.FILM_ACTOR).where(left.ACTOR_ID.eq(Tables.FILM_ACTOR.ACTOR_ID).and(Tables.FILM_ACTOR.FILM_ID.eq(right.FILM_ID)))
        )

        relationshipName == "stores" && left is Film && right is Store -> DSL.exists(
            DSL.selectOne().from(Tables.INVENTORY).where(left.FILM_ID.eq(Tables.INVENTORY.FILM_ID).and(Tables.INVENTORY.STORE_ID.eq(right.STORE_ID)))
        )

        relationshipName == "films" && left is Store && right is Film -> DSL.exists(
            DSL.selectOne().from(Tables.INVENTORY).where(left.STORE_ID.eq(Tables.INVENTORY.STORE_ID).and(Tables.INVENTORY.FILM_ID.eq(right.FILM_ID)))
        )

        relationshipName == "language" && left is Film && right is Language -> left.LANGUAGE_ID.eq(right.LANGUAGE_ID)
        relationshipName == "original_language" && left is Film && right is Language -> left.ORIGINAL_LANGUAGE_ID.eq(right.LANGUAGE_ID)

        relationshipName == "inventories" && left is Store && right is Inventory -> left.STORE_ID.eq(right.STORE_ID)

        relationshipName == "film" && left is Inventory && right is Film -> left.FILM_ID.eq(right.FILM_ID)

        relationshipName == "categories" && left is Film && right is Category -> DSL.exists(
            DSL.selectOne().from(Tables.FILM_CATEGORY)
                .where(left.FILM_ID.eq(Tables.FILM_CATEGORY.FILM_ID).and(Tables.FILM_CATEGORY.CATEGORY_ID.eq(right.CATEGORY_ID)))
        )

        relationshipName == "films" && left is Category && right is Film -> DSL.exists(
            DSL.selectOne().from(Tables.FILM_CATEGORY)
                .where(left.CATEGORY_ID.eq(Tables.FILM_CATEGORY.CATEGORY_ID).and(Tables.FILM_CATEGORY.FILM_ID.eq(right.FILM_ID)))
        )

        relationshipName == "parent_category" && left is Category && right is Category -> left.PARENT_CATEGORY_ID.eq(right.CATEGORY_ID)

        relationshipName == "subcategories" && left is Category && right is Category -> left.CATEGORY_ID.eq(right.PARENT_CATEGORY_ID)

        else -> null
    }

fun getRelatedTable(relationshipName: String, from: Table<*>): Table<*>? =
    when {
        relationshipName == "actors" && from is Film -> Tables.ACTOR
        relationshipName == "films" && from is Actor -> Tables.FILM
        relationshipName == "stores" && from is Film -> Tables.STORE
        relationshipName == "films" && from is Store -> Tables.FILM
        relationshipName == "language" && from is Film -> Tables.LANGUAGE
        relationshipName == "original_language" && from is Film -> Tables.LANGUAGE
        relationshipName == "inventories" && from is Store -> Tables.INVENTORY
        relationshipName == "film" && from is Inventory -> Tables.FILM
        relationshipName == "categories" && from is Film -> Tables.CATEGORY
        relationshipName == "films" && from is Category -> Tables.FILM
        relationshipName == "parent_category" && from is Category -> Tables.CATEGORY
        relationshipName == "subcategories" && from is Category -> Tables.CATEGORY
        else -> null
    }