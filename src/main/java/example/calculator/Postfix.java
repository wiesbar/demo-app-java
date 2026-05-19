package example.calculator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

final class Postfix {

  private final List<PostfixToken> expression;
  private final Deque<Result> values = new ArrayDeque<>();

  private Postfix(List<PostfixToken> expression) {
    this.expression = expression;
  }

  static double calculate(List<PostfixToken> expression) {
    return new Postfix(expression).calculate();
  }

  private double calculate() {
    for (var token : expression) {
      switch (token) {
        case Operand operand -> values.addLast(new Value(operand));
        case UnaryOperator.Negate negate -> applyNegate(negate);
        case BinaryOperator binaryOperator -> applyBinary(binaryOperator);
      }
    }
    return calculatedValue();
  }

  private double calculatedValue() {
    return switch (values.size()) {
      case 0 -> throw new InvalidArithmeticExpressionException("Invalid empty expression.");
      case 1 -> values.peekLast().value();
      default ->
          throw new InvalidArithmeticExpressionException(
              Messages.unexpectedOperand(values.peekLast().positionInExpression()));
    };
  }

  private void applyNegate(UnaryOperator.Negate operator) {
    var value = values.pollLast();
    if (value == null) {
      throw new InvalidArithmeticExpressionException(Messages.missingOperand(operator));
    }
    values.addLast(new Negation(operator, value));
  }

  private void applyBinary(BinaryOperator operator) {
    if (values.size() < 2) {
      throw new InvalidArithmeticExpressionException(Messages.missingOperand(operator));
    }
    var right = values.removeLast();
    var left = values.removeLast();
    var result =
        switch (operator) {
          case BinaryOperator.Plus ignored -> new Sum(left, right);
          case BinaryOperator.Minus ignored -> new Difference(left, right);
          case BinaryOperator.Times ignored -> new Product(left, right);
        };
    values.addLast(result);
  }
}
