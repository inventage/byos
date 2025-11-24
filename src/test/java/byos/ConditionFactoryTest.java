package byos;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ConditionFactoryTest {

    @ParameterizedTest
    @MethodSource("extractArrayValueParameters")
    public void extractArrayValue(ArrayNode input, Object[] expected) {
        // given
        ConditionFactory conditionFactory = new ConditionFactory();
        // when
        Object[] objects = conditionFactory.extractArrayValue(input);
        // then
        Assertions.assertEquals(expected.length, objects.length);
    }

    public static Stream<Arguments> extractArrayValueParameters() {
        return Stream.of(
                Arguments.of(JsonNodeFactory.instance.arrayNode(), new Object[]{}),
                Arguments.of(JsonNodeFactory.instance.arrayNode().add(1), new Object[]{1}),
                Arguments.of(JsonNodeFactory.instance.arrayNode().add("1"), new Object[]{"1"}),
                Arguments.of(JsonNodeFactory.instance.arrayNode().add(1).add(2).add(3).add(4).add(5), new Object[]{1,2,3,4,5}),
                Arguments.of(JsonNodeFactory.instance.arrayNode().add("1").add("2").add("3").add("4").add("5"), new Object[]{"1","2","3","4","5"})
        );
    }

}
