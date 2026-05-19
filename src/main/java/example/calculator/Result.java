package example.calculator;

sealed interface Result permits Value, Sum, Difference, Product, Negation {
  double value();

  int positionInExpression();
}

final class Value implements Result {
  private final Operand operand;

  Value(Operand operand) {
    this.operand = operand;
  }

  @Override
  public double value() {
    return operand.value();
  }

  @Override
  public int positionInExpression() {
    return operand.positionInExpression();
  }
}

final class Sum implements Result {
  private final Result left;
  private final Result right;

  Sum(Result left, Result right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public double value() {
    return left.value() + right.value();
  }

  @Override
  public int positionInExpression() {
    return right.positionInExpression();
  }
}

final class Difference implements Result {
  private final Result left;
  private final Result right;

  Difference(Result left, Result right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public double value() {
    return left.value() - right.value();
  }

  @Override
  public int positionInExpression() {
    return right.positionInExpression();
  }
}

final class Product implements Result {
  private final Result left;
  private final Result right;

  Product(Result left, Result right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public double value() {
    return left.value() * right.value();
  }

  @Override
  public int positionInExpression() {
    return right.positionInExpression();
  }
}

final class Negation implements Result {
  private final UnaryOperator.Negate operator;
  private final Result operand;

  Negation(UnaryOperator.Negate operator, Result operand) {
    this.operator = operator;
    this.operand = operand;
  }

  @Override
  public double value() {
    return -operand.value();
  }

  @Override
  public int positionInExpression() {
    return operator.positionInExpression();
  }
}
