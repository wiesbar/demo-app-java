package example.calculator;

sealed interface Token permits InfixToken, PostfixToken {
  int positionInExpression();
}

sealed interface InfixToken extends Token permits Operand, Operator, Parenthesis {}

sealed interface PostfixToken extends Token permits Operand, Operator {}

sealed interface Operator extends InfixToken, PostfixToken permits BinaryOperator, UnaryOperator {
  char symbol();

  default String locationInExpression() {
    return "'"
        + symbol()
        + "' operator at position '"
        + positionInExpression()
        + "' in expression.";
  }
}

record Operand(double value, int positionInExpression) implements InfixToken, PostfixToken {}

sealed interface BinaryOperator extends Operator
    permits BinaryOperator.Plus, BinaryOperator.Minus, BinaryOperator.Times {
  int precedence();

  record Plus(int positionInExpression) implements BinaryOperator {
    @Override
    public int precedence() {
      return 1;
    }

    @Override
    public char symbol() {
      return '+';
    }
  }

  record Minus(int positionInExpression) implements BinaryOperator {
    @Override
    public int precedence() {
      return 1;
    }

    @Override
    public char symbol() {
      return '-';
    }
  }

  record Times(int positionInExpression) implements BinaryOperator {
    @Override
    public int precedence() {
      return 2;
    }

    @Override
    public char symbol() {
      return '*';
    }
  }
}

sealed interface UnaryOperator extends Operator permits UnaryOperator.Negate {

  record Negate(int positionInExpression) implements UnaryOperator {
    @Override
    public char symbol() {
      return '-';
    }
  }
}

sealed interface Parenthesis extends InfixToken permits Parenthesis.Left, Parenthesis.Right {

  record Left(int positionInExpression) implements Parenthesis {}

  record Right(int positionInExpression) implements Parenthesis {}
}
