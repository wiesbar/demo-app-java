package example.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PostfixTest {

  static Stream<Arguments> validExpressions() {
    return Stream.of(
        arguments(
            "1 2 +",
            List.of(new Operand(1.0, 0), new Operand(2.0, 4), new BinaryOperator.Plus(2)),
            3.0),
        arguments(
            "5 3 -",
            List.of(new Operand(5.0, 0), new Operand(3.0, 4), new BinaryOperator.Minus(2)),
            2.0),
        arguments(
            "2 3 *",
            List.of(new Operand(2.0, 0), new Operand(3.0, 4), new BinaryOperator.Times(2)),
            6.0),
        arguments("1 negate", List.of(new Operand(1.0, 1), new UnaryOperator.Negate(0)), -1.0),
        arguments("1", List.of(new Operand(1.0, 0)), 1.0),
        arguments(
            "2 3 4 * +",
            List.of(
                new Operand(2.0, 0),
                new Operand(3.0, 4),
                new Operand(4.0, 8),
                new BinaryOperator.Times(6),
                new BinaryOperator.Plus(2)),
            14.0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validExpressions")
  void shouldEvaluatePostfix(String description, List<PostfixToken> postfix, double expected) {
    assertThat(Postfix.calculate(postfix)).isEqualTo(expected);
  }

  static Stream<Arguments> invalidExpressions() {
    return Stream.of(
        arguments("empty", List.of(), "Invalid empty expression."),
        arguments(
            "two operands",
            List.of(new Operand(1.0, 0), new Operand(2.0, 6)),
            "Unexpected operand at position '6' in expression."),
        arguments(
            "binary without operands",
            List.of(new BinaryOperator.Plus(2)),
            "Missing operand for '+' operator at position '2' in expression."),
        arguments(
            "negate without operand",
            List.of(new UnaryOperator.Negate(0)),
            "Missing operand for '-' operator at position '0' in expression."));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidExpressions")
  void shouldRejectInvalidPostfix(
      String description, List<PostfixToken> postfix, String expectedMessage) {
    assertThatThrownBy(() -> Postfix.calculate(postfix))
        .isInstanceOf(InvalidArithmeticExpressionException.class)
        .hasMessage(expectedMessage)
        .hasNoCause();
  }
}
