package example.calculator;

public class InvalidArithmeticExpressionException extends RuntimeException {

  public InvalidArithmeticExpressionException(String message) {
    super(message);
  }
}
