package example.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class InfixParserTest {

  static Stream<Arguments> validExpressions() {
    return Stream.of(
        arguments(
            "1 + 2", List.of(new Operand(1.0, 0), new BinaryOperator.Plus(2), new Operand(2.0, 4))),
        arguments("-1", List.of(new UnaryOperator.Negate(0), new Operand(1.0, 1))),
        arguments(
            "1 - -2",
            List.of(
                new Operand(1.0, 0),
                new BinaryOperator.Minus(2),
                new UnaryOperator.Negate(4),
                new Operand(2.0, 5))),
        arguments(
            "2 * 3",
            List.of(new Operand(2.0, 0), new BinaryOperator.Times(2), new Operand(3.0, 4))),
        arguments(
            "(1)", List.of(new Parenthesis.Left(0), new Operand(1.0, 1), new Parenthesis.Right(2))),
        arguments(".5", List.of(new Operand(0.5, 0))),
        arguments(
            "  7+8  ",
            List.of(new Operand(7.0, 2), new BinaryOperator.Plus(3), new Operand(8.0, 4))),
        arguments("", List.of()));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validExpressions")
  void shouldTokenizeValidExpression(String expression, List<InfixToken> expected) {
    assertThat(InfixParser.toInfix(expression)).isEqualTo(expected);
  }

  static Stream<Arguments> invalidExpressions() {
    return Stream.of(
        arguments("a + b", "Invalid character 'a' at position '0' in expression."),
        arguments("1..2", "Invalid number literal '1..2' at position '0' in expression."));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidExpressions")
  void shouldRejectInvalidExpression(String expression, String expectedMessage) {
    assertThatThrownBy(() -> InfixParser.toInfix(expression))
        .isInstanceOf(InvalidArithmeticExpressionException.class)
        .hasMessage(expectedMessage)
        .hasNoCause();
  }
}
