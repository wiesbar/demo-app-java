package example.calculator;

import java.util.ArrayList;
import java.util.List;

final class InfixParser {

  private final Cursor cursor;
  private final List<InfixToken> tokens = new ArrayList<>();

  private InfixParser(String expression) {
    this.cursor = new Cursor(expression);
  }

  static List<InfixToken> toInfix(String expression) {
    return new InfixParser(expression).parse();
  }

  private List<InfixToken> parse() {
    while (cursor.hasNextChar()) {
      readNext();
    }
    return List.copyOf(tokens);
  }

  private void readNext() {
    switch (cursor.currentChar()) {
      case '(' -> consume(new Parenthesis.Left(cursor.index()));
      case ')' -> consume(new Parenthesis.Right(cursor.index()));
      case '*' -> consume(new BinaryOperator.Times(cursor.index()));
      case '+' -> consume(new BinaryOperator.Plus(cursor.index()));
      case '-' -> readMinus();
      default -> readWhitespaceOrNumber();
    }
  }

  private void readWhitespaceOrNumber() {
    if (cursor.isWhitespace()) {
      cursor.advance();
    } else if (cursor.isDigitOrPoint()) {
      readNumber();
    } else {
      throw new InvalidArithmeticExpressionException(
          Messages.invalidCharacter(cursor.currentChar(), cursor.index()));
    }
  }

  private void consume(InfixToken token) {
    tokens.add(token);
    cursor.advance();
  }

  private void readMinus() {
    consume(
        isAtUnaryPosition()
            ? new UnaryOperator.Negate(cursor.index())
            : new BinaryOperator.Minus(cursor.index()));
  }

  private boolean isAtUnaryPosition() {
    var previous = tokens.isEmpty() ? null : tokens.getLast();
    return previous == null || previous instanceof Operator || previous instanceof Parenthesis.Left;
  }

  private void readNumber() {
    var start = cursor.index();
    while (cursor.hasNextChar() && cursor.isDigitOrPoint()) {
      cursor.advance();
    }
    tokens.add(asOperand(start));
  }

  private Operand asOperand(int position) {
    String literal = cursor.literal(position);
    try {
      double number = Double.parseDouble(literal);
      return new Operand(number, position);
    } catch (NumberFormatException e) {
      throw new InvalidArithmeticExpressionException(
          Messages.invalidNumberLiteral(literal, position));
    }
  }

  private static final class Cursor {

    private final String expression;
    private int index = 0;

    Cursor(String expression) {
      this.expression = expression;
    }

    int index() {
      return index;
    }

    void advance() {
      index++;
    }

    char currentChar() {
      return expression.charAt(index);
    }

    boolean hasNextChar() {
      return index < expression.length();
    }

    String literal(int start) {
      return expression.substring(start, index);
    }

    boolean isWhitespace() {
      return Character.isWhitespace(currentChar());
    }

    boolean isDigitOrPoint() {
      var c = currentChar();
      return Character.isDigit(c) || c == '.';
    }
  }
}
