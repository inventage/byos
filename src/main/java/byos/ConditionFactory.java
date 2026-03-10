package byos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import graphql.language.*;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static byos.ByosConstants.*;

// https://www.graphql-java.com/documentation/data-mapping#scalars
public class ConditionFactory {

    public static Condition getWhereCondition(
            Argument whereArgument,
            Map<String, JsonNode> variables,
            Table<?> table,
            TableAndConditionService tableAndConditionService
    ) {
        return getCondition(getWhereObject(whereArgument.getValue()), variables, table, tableAndConditionService);
    }

    public static Condition getCondition(ObjectValue objectValue, Map<String, JsonNode> variables, Table<?> table, TableAndConditionService tableAndConditionService) {
        switch (objectValue.getObjectFields().size()) {
            case 0: {
                return DSL.noCondition();
            }
            case 1: {
                return getCondition(objectValue.getObjectFields().get(0), variables, table, tableAndConditionService);
            }
            default: {
                // multiple fields conditions are "add" concatenated by default
                return DSL.and(objectValue.getObjectFields().stream()
                        .map(objectField -> getCondition(objectField, variables, table, tableAndConditionService))
                        .collect(Collectors.toSet()));
            }
        }
    }

    private static ObjectValue getWhereObject(Value value) {
        if (value instanceof ObjectValue) {
            return (ObjectValue) value;
        }
        if (value instanceof IntValue) {
            final ObjectValue build = ObjectValue.newObjectValue().objectField(ObjectField.newObjectField().name("value").value(value).build()).build();
            return build;
        }
        throw new IllegalArgumentException("Value of whereArgument must be an object");
    }

    private static Condition getCondition(ObjectField objectField, Map<String, JsonNode> variables, Table<?> table, TableAndConditionService tableAndConditionService) {
        final String name = objectField.getName();
        switch (name) {
            case CONDITION_AND: {
                Value rawValue = objectField.getValue();
                if (rawValue instanceof VariableReference) {
                    JsonNode resolved = variables.get(((VariableReference) rawValue).getName());
                    return DSL.and(List.of(getConditionFromJsonNode(resolved, variables, table, tableAndConditionService)));
                }
                return DSL.and(asArrayValue(rawValue).getValues().stream()
                        .map(value -> resolveToCondition(value, variables, table, tableAndConditionService))
                        .collect(Collectors.toList()));
            }
            case CONDITION_OR: {
                Value rawValue = objectField.getValue();
                if (rawValue instanceof VariableReference) {
                    JsonNode resolved = variables.get(((VariableReference) rawValue).getName());
                    return DSL.or(List.of(getConditionFromJsonNode(resolved, variables, table, tableAndConditionService)));
                }
                return DSL.or(asArrayValue(rawValue).getValues().stream()
                        .map(value -> resolveToCondition(value, variables, table, tableAndConditionService))
                        .collect(Collectors.toList()));
            }
            case CONDITION_NOT: {
                return DSL.not(getCondition((ObjectValue) objectField.getValue(), variables, table, tableAndConditionService)); 
            }
            default: {

                final Field field = table.field(name);
                if (field != null) {
                    return getCondition(field, variables, objectField.getValue());
                }
                // if the name does not match with any columns in the current table and it is is a nested object
                // (not a scalar)
                if (tableAndConditionService != null && objectField.getValue() instanceof ObjectValue) {
                    ObjectValue nestedWhere = (ObjectValue) objectField.getValue();

                    Table<?> relatedTable = tableAndConditionService.getRelatedTable(name, table);
                    if (relatedTable != null) {
                        Condition joinCondition = tableAndConditionService.getConditionFor(name, table, relatedTable);
                        Condition nestedCondition = getCondition(nestedWhere, variables, relatedTable, tableAndConditionService);
                        return DSL.exists(
                                DSL.selectOne()
                                        .from(relatedTable)
                                        .where(joinCondition)
                                        .and(nestedCondition)
                        );
                    }
                }
            }
        }
        return DSL.noCondition();
    }

    private static Condition resolveToCondition(Value value, Map<String, JsonNode> variables, Table<?> table, TableAndConditionService tableAndConditionService) {
        if (value instanceof ObjectValue) {
            return getCondition((ObjectValue) value, variables, table, tableAndConditionService);
        } else if (value instanceof VariableReference) {
            JsonNode resolved = variables.get(((VariableReference) value).getName());
            return getConditionFromJsonNode(resolved, variables, table, tableAndConditionService);
        }
        throw new IllegalArgumentException("Unsupported value type in condition array: " + value.getClass());
    }

    private static Condition getConditionFromJsonNode(JsonNode node, Map<String, JsonNode> variables, Table<?> table, TableAndConditionService tableAndConditionService) {
        if (node.isObject()) {
            ObjectValue.Builder builder = ObjectValue.newObjectValue();
            node.fields().forEachRemaining(entry -> {
                builder.objectField(ObjectField.newObjectField()
                        .name(entry.getKey())
                        .value(jsonNodeToValue(entry.getValue()))
                        .build());
            });
            return getCondition(builder.build(), variables, table, tableAndConditionService);
        }
        return DSL.noCondition();
    }

    private static Value jsonNodeToValue(JsonNode node) {
        if (node.isTextual()) {
            return StringValue.newStringValue(node.asText()).build();
        } else if (node.isInt() || node.isLong()) {
            return IntValue.newIntValue(BigInteger.valueOf(node.asLong())).build();
        } else if (node.isFloat() || node.isDouble()) {
            return FloatValue.newFloatValue(BigDecimal.valueOf(node.asDouble())).build();
        } else if (node.isBoolean()) {
            return BooleanValue.newBooleanValue(node.asBoolean()).build();
        } else if (node.isArray()) {
            ArrayValue.Builder arrayBuilder = ArrayValue.newArrayValue();
            node.forEach(element -> arrayBuilder.value(jsonNodeToValue(element)));
            return arrayBuilder.build();
        } else if (node.isObject()) {
            ObjectValue.Builder objBuilder = ObjectValue.newObjectValue();
            node.fields().forEachRemaining(entry ->
                    objBuilder.objectField(
                            ObjectField.newObjectField()
                                    .name(entry.getKey())
                                    .value(jsonNodeToValue(entry.getValue()))
                                    .build()
                    )
            );
            return objBuilder.build();
        } else if (node.isNull()) {
            return NullValue.newNullValue().build();
        }
        throw new IllegalArgumentException("Unsupported JsonNode type: " + node.getNodeType());
    }

    private static ArrayValue asArrayValue(Value value) {
        if (value instanceof ArrayValue) {
            return (ArrayValue) value;
        }
        return null;
    }

    protected static Condition getCondition(Field field, Map<String, JsonNode> variables, Value objectValue) {
        if (!(objectValue instanceof ObjectValue)) {
            throw new IllegalArgumentException("Handling for value not yet implemented: " + objectValue);
        }

        List<ObjectField> objectFields = ((ObjectValue) objectValue).getObjectFields();
        Condition combinedCondition = null;

        for (ObjectField objectField : objectFields) {
            Object value = extractValue(objectField.getValue(), variables);
            StringComparisonOperator.valueOf(objectField.getName());
            Condition condition = switch (StringComparisonOperator.valueOf(objectField.getName())) {
                case _eq -> field.eq(value);
                case _neq -> field.ne(value);
                case _lt -> field.lt(value);
                case _lte -> field.lessOrEqual(value);
                case _gt -> field.gt(value);
                case _gte -> field.greaterOrEqual(value);
                case _like -> field.like(value.toString());
                case _ilike -> field.likeIgnoreCase(value.toString());
                case _regex -> field.likeRegex(value.toString());
                case _iregex -> field.likeIgnoreCase(value.toString());
                case _in -> field.in(value);
                case _nin -> field.notIn(value);
                case _is_null -> field.isNull();
                default -> throw new IllegalArgumentException("nyi");
            };


            combinedCondition = (combinedCondition == null) ? condition : combinedCondition.and(condition);
        }
        return combinedCondition;
    }

    public static Object extractValue(Value value, Map<String, JsonNode> variables) {
        if (value instanceof StringValue) {
            return ((StringValue) value).getValue();
        } else if (value instanceof IntValue) {
            return ((IntValue) value).getValue();
        } else if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue();
        } else if (value instanceof ArrayValue) {
            return ((ArrayValue) value).getValues().stream().map(v -> extractValue(v, variables)).collect(Collectors.toList());
        } else if (value instanceof ObjectValue) {
            return ((ObjectValue) value).getObjectFields();
        } else if (value instanceof VariableReference) {
            final JsonNode jsonNode = variables.get(((VariableReference) value).getName());
            if (jsonNode != null) {
                if (jsonNode.isArray()) {
                    return extractArrayValue((ArrayNode) jsonNode);
                } else if (jsonNode.isObject()) {
                    return jsonNode;
                } else {
                    return jsonNode.asText();
                }
            }
            return null;
        }

        throw new IllegalArgumentException("nyi");
    }

    protected static List extractArrayValue(ArrayNode arrayNode) {
        ObjectMapper mapper = new ObjectMapper();
        return StreamSupport.stream(arrayNode.spliterator(), false)
                .map(jsonNode -> mapper.convertValue(jsonNode, Object.class)).collect(Collectors.toList());
    }

    public static IntValue extractIntValue(Value value, Map<String, JsonNode> variables) {
        if (value == null) {
            return null;
        }
        if (value instanceof IntValue) {
            return (IntValue) value;
        }
        final Object extractedValue = extractValue(value, variables);
        if (extractedValue instanceof BigDecimal) {
            return new IntValue((BigInteger) extractedValue);
        } else if (extractedValue instanceof String) {
            return new IntValue(new BigInteger((String) extractedValue));
        }
        return null;
    }
}
