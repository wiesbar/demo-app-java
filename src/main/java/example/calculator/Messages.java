package example.calculator;

final class Messages {

  private Messages() {}

  static String invalidCharacter(char character, int position) {
    return "Invalid character '" + character + "'" + atPosition(position);
  }

  static String invalidNumberLiteral(String literal, int position) {
    return "Invalid number literal '" + literal + "'" + atPosition(position);
  }

  static String unexpectedOperand(int position) {
    return "Unexpected operand" + atPosition(position);
  }

  static String unmatched(char parenthesis, int position) {
    return "Unmatched '" + parenthesis + "'" + atPosition(position);
  }

  static String emptyParentheses(int position) {
    return "Empty parentheses" + atPosition(position);
  }

  static String missingOperand(Operator operator) {
    return "Missing operand for " + operator.locationInExpression();
  }

  private static String atPosition(int position) {
    return " at position '" + position + "' in expression.";
  }
}
