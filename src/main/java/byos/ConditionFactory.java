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

import static byos.ByosConstants.*;

// https://www.graphql-java.com/documentation/data-mapping#scalars
public class ConditionFactory {

    public static Condition getWhereCondition(Argument whereArgument, Map<String, JsonNode> variables, Table<?> table) {
        return getCondition(getWhereObject(whereArgument.getValue()), variables, table);
    }

    public static Condition getCondition(ObjectValue objectValue, Map<String, JsonNode> variables, Table<?> table) {
        switch (objectValue.getObjectFields().size()) {
            case 0: {
                return DSL.noCondition();
            }
            case 1: {
                return getCondition(objectValue.getObjectFields().get(0), variables, table);
            }
            default: {
                // multiple fields conditions are "add" concatenated by default
                return DSL.and(objectValue.getObjectFields().stream()
                        .map(objectField -> getCondition(objectField, variables, table))
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

    private static Condition getCondition(ObjectField objectField, Map<String, JsonNode> variables, Table<?> table) {
        final String name = objectField.getName();
        switch (name) {
            case CONDITION_AND: {
                return DSL.and(asArrayValue(objectField.getValue()).getValues().stream()
                        .map(objectValue -> getCondition((ObjectValue) objectValue, variables, table))
                        .collect(Collectors.toSet()));
            }
            case CONDITION_OR: {
                return DSL.or(asArrayValue(objectField.getValue()).getValues().stream()
                        .map(objectValue -> getCondition((ObjectValue) objectValue, variables, table))
                        .collect(Collectors.toSet()));
            }
            case CONDITION_NOT: {
                return DSL.not(getCondition((ObjectValue) objectField.getValue(), variables, table));
            }
            default: {
                final Field field = table.field(name);
                if (field != null) {
                    return getCondition(field, variables, objectField.getValue());
                }
            }
        }
        return DSL.noCondition();
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
            Condition condition;
            switch (StringComparisonOperator.valueOf(objectField.getName())) {
                case _eq:
                    condition = field.eq(value);
                    break;
                case _neq:
                    condition = field.ne(value);
                    break;
                case _lt:
                    condition = field.lt(value);
                    break;
                case _lte:
                    condition = field.lessOrEqual(value);
                    break;
                case _gt:
                    condition = field.gt(value);
                    break;
                case _gte:
                    condition = field.greaterOrEqual(value);
                    break;
                case _like:
                    condition = field.like(value.toString());
                    break;
                case _ilike, _iregex:
                    condition = field.likeIgnoreCase(value.toString());
                    break;
                case _regex:
                    condition = field.likeRegex(value.toString());
                    break;
                case _in:
                    condition = field.in((Object[]) value);
                    break;
                case _nin:
                    condition = field.notIn((Object[]) value);
                    break;
                case _is_null:
                    condition = field.isNull();
                    break;
                default:
                    throw new IllegalArgumentException("nyi");
            }

            combinedCondition = (combinedCondition == null) ? condition : combinedCondition.and(condition);
        }
        return combinedCondition;
    }

    public static Object extractValue(Value value, Map<String, JsonNode> variables) {
        if (value instanceof StringValue) {
            return ((StringValue)value).getValue();
        } else if (value instanceof IntValue) {
            return ((IntValue)value).getValue();
        } else if (value instanceof BooleanValue) {
            return ((BooleanValue)value).isValue();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue();
        } else if (value instanceof ArrayValue) {
            return ((ArrayValue)value).getValues().stream().map(v -> extractValue(v, variables)).collect(Collectors.toList());
        } else if (value instanceof ObjectValue) {
            return ((ObjectValue)value).getObjectFields();
        } else if (value instanceof VariableReference) {
            final JsonNode jsonNode = variables.get(((VariableReference) value).getName());
            if (jsonNode != null) {
                if (jsonNode.isArray()) {
                    return extractArrayValue((ArrayNode) jsonNode);
                }
                else {
                    return jsonNode.asText();
                }
            }
            return null;
        }
        throw new IllegalArgumentException("nyi");
    }

    protected static Object[] extractArrayValue(ArrayNode arrayNode) {
        ObjectMapper mapper = new ObjectMapper();
        Object[] objects = mapper.convertValue(arrayNode, Object[].class);
        return objects;
    }

    public static IntValue extractIntValue(Value value, Map<String, JsonNode> variables) {
        if (value == null) {
            return null;
        }
        if (value instanceof  IntValue) {
            return (IntValue) value;
        }
        final Object extractedValue = extractValue(value, variables);
        if (extractedValue instanceof BigDecimal) {
            return new IntValue((BigInteger) extractedValue);
        }
        else if (extractedValue instanceof String) {
            return new IntValue(new BigInteger((String)extractedValue));
        }
        return null;
    }
}
