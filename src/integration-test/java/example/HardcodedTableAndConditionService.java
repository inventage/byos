package example;

import byos.InternalQueryNode;
import byos.TableAndConditionService;
import db.jooq.generated.Public;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.Table;

public class HardcodedTableAndConditionService implements TableAndConditionService {
    @NotNull
    @Override
    public Table<? extends Record> getTableWithAliasFor(@NotNull InternalQueryNode.Relation relation) {
        return Public.PUBLIC.getTable(relation.getFieldTypeInfo().getRelationName().toLowerCase());
    }

    @Nullable
    @Override
    public Condition getConditionFor(@NotNull String relationshipName, @NotNull Table<?> left, @NotNull Table<?> right) {
        return ConfigKt.getConditionForRelationship(relationshipName, left, right);
    }
}
