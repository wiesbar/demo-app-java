package example.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ArithmeticExpressionCalculatorTest {

  private final ArithmeticExpressionCalculator calculator =
      new DefaultArithmeticExpressionCalculator();

  static Stream<Arguments> validExpressions() {
    return Stream.of(
        arguments("1 + 2", 3.0),
        arguments("5 - 3", 2.0),
        arguments("1.5 + 2.25", 3.75),
        arguments("1 + 2 + 3", 6.0),
        arguments("10 - 3 - 2", 5.0),
        arguments("1 - 2 + 3", 2.0),
        arguments("-1 + 2", 1.0),
        arguments("1 - -2", 3.0),
        arguments("(1 + 2) - 3", 0.0),
        arguments("1 - (2 - 3)", 2.0),
        arguments("((1))", 1.0),
        arguments("-(1 + 2)", -3.0),
        arguments("1 + -(2 - 3)", 2.0),
        arguments("  7+8  ", 15.0),
        arguments("1", 1.0),
        arguments("(42.5)", 42.5),
        arguments("-3", -3.0),
        arguments(".5 + 1", 1.5),
        arguments("(-1 + 2)", 1.0),
        arguments("--1", 1.0),
        arguments("2 * 3", 6.0),
        arguments("2 + 3 * 4", 14.0),
        arguments("2 * 3 + 4", 10.0),
        arguments("(2 + 3) * 4", 20.0),
        arguments("2 * 3 * 4", 24.0),
        arguments("-2 * 3", -6.0),
        arguments("2 * -3", -6.0));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validExpressions")
  void shouldEvaluateValidExpression(String expression, double expected) {
    assertThat(calculator.calculate(expression)).isEqualTo(expected);
  }

  static Stream<Arguments> invalidExpressions() {
    return Stream.of(
        arguments("", "Invalid empty expression."),
        arguments("1 +", "Missing operand for '+' operator at position '2' in expression."),
        arguments("1 + -", "Missing operand for '+' operator at position '2' in expression."),
        arguments("-", "Missing operand for '-' operator at position '0' in expression."),
        arguments("*", "Missing operand for '*' operator at position '0' in expression."),
        arguments("+", "Missing operand for '+' operator at position '0' in expression."),
        arguments("+3 - 1", "Missing operand for '+' operator at position '0' in expression."),
        arguments("1 + 2 3", "Unexpected operand at position '6' in expression."),
        arguments("a + b", "Invalid character 'a' at position '0' in expression."),
        arguments("(1 + 2", "Unmatched '(' at position '0' in expression."),
        arguments("1 + 2)", "Unmatched ')' at position '5' in expression."),
        arguments("()", "Empty parentheses at position '0' in expression."),
        arguments(")", "Unmatched ')' at position '0' in expression."),
        arguments("1..2", "Invalid number literal '1..2' at position '0' in expression."));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidExpressions")
  void shouldRejectInvalidExpression(String expression, String expectedMessage) {
    assertThatThrownBy(() -> calculator.calculate(expression))
        .isInstanceOf(InvalidArithmeticExpressionException.class)
        .hasMessage(expectedMessage)
        .hasNoCause();
  }
}
