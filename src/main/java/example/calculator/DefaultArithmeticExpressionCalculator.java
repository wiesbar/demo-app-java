package example.calculator;

import static example.calculator.InfixParser.toInfix;
import static example.calculator.PostfixParser.toPostfix;

public final class DefaultArithmeticExpressionCalculator implements ArithmeticExpressionCalculator {

  @Override
  public double calculate(String expression) {
    var infix = toInfix(expression);
    var postfix = toPostfix(infix);
    return Postfix.calculate(postfix);
  }
}
