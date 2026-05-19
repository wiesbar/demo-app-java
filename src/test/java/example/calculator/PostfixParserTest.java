package example.calculator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PostfixParserTest {

  static Stream<Arguments> validExpressions() {
    return Stream.of(
        arguments(
            "1 + 2",
            List.of(new Operand(1.0, 0), new BinaryOperator.Plus(2), new Operand(2.0, 4)),
            List.of(new Operand(1.0, 0), new Operand(2.0, 4), new BinaryOperator.Plus(2))),
        arguments(
            "2 + 3 * 4",
            List.of(
                new Operand(2.0, 0),
                new BinaryOperator.Plus(2),
                new Operand(3.0, 4),
                new BinaryOperator.Times(6),
                new Operand(4.0, 8)),
            List.of(
                new Operand(2.0, 0),
                new Operand(3.0, 4),
                new Operand(4.0, 8),
                new BinaryOperator.Times(6),
                new BinaryOperator.Plus(2))),
        arguments(
            "(1 + 2) * 3",
            List.of(
                new Parenthesis.Left(0),
                new Operand(1.0, 1),
                new BinaryOperator.Plus(3),
                new Operand(2.0, 5),
                new Parenthesis.Right(6),
                new BinaryOperator.Times(8),
                new Operand(3.0, 10)),
            List.of(
                new Operand(1.0, 1),
                new Operand(2.0, 5),
                new BinaryOperator.Plus(3),
                new Operand(3.0, 10),
                new BinaryOperator.Times(8))),
        arguments(
            "-1",
            List.of(new UnaryOperator.Negate(0), new Operand(1.0, 1)),
            List.of(new Operand(1.0, 1), new UnaryOperator.Negate(0))),
        arguments(
            "((1))",
            List.of(
                new Parenthesis.Left(0),
                new Parenthesis.Left(1),
                new Operand(1.0, 2),
                new Parenthesis.Right(3),
                new Parenthesis.Right(4)),
            List.of(new Operand(1.0, 2))));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validExpressions")
  void shouldConvertToPostfix(
      String description, List<InfixToken> infix, List<PostfixToken> expected) {
    assertThat(PostfixParser.toPostfix(infix)).isEqualTo(expected);
  }

  static Stream<Arguments> invalidExpressions() {
    return Stream.of(
        arguments(
            "(1 + 2",
            List.of(
                new Parenthesis.Left(0),
                new Operand(1.0, 1),
                new BinaryOperator.Plus(3),
                new Operand(2.0, 5)),
            "Unmatched '(' at position '0' in expression."),
        arguments(
            "1 + 2)",
            List.of(
                new Operand(1.0, 0),
                new BinaryOperator.Plus(2),
                new Operand(2.0, 4),
                new Parenthesis.Right(5)),
            "Unmatched ')' at position '5' in expression."),
        arguments(
            "()",
            List.of(new Parenthesis.Left(0), new Parenthesis.Right(1)),
            "Empty parentheses at position '0' in expression."),
        arguments(
            ")",
            List.of(new Parenthesis.Right(0)),
            "Unmatched ')' at position '0' in expression."));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidExpressions")
  void shouldRejectInvalidInfix(
      String description, List<InfixToken> infix, String expectedMessage) {
    assertThatThrownBy(() -> PostfixParser.toPostfix(infix))
        .isInstanceOf(InvalidArithmeticExpressionException.class)
        .hasMessage(expectedMessage)
        .hasNoCause();
  }
}
