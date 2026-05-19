package example.calculator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.jspecify.annotations.Nullable;

final class PostfixParser {

  private final List<InfixToken> infix;
  private final List<PostfixToken> output = new ArrayList<>();
  private final Deque<InfixToken> stack = new ArrayDeque<>();
  @Nullable private InfixToken previous;

  private PostfixParser(List<InfixToken> infix) {
    this.infix = infix;
  }

  static List<PostfixToken> toPostfix(List<InfixToken> infix) {
    return new PostfixParser(infix).parse();
  }

  private List<PostfixToken> parse() {
    for (var token : infix) {
      switch (token) {
        case Operand operand -> output.add(operand);
        case BinaryOperator binaryOperator -> pushBinaryOperator(binaryOperator);
        case UnaryOperator unaryOperator -> pushUnaryOperator(unaryOperator);
        case Parenthesis.Left left -> stack.addLast(left);
        case Parenthesis.Right right -> handleRightParenthesis(right);
      }
      previous = token;
    }
    drainRemainingOperators();
    return List.copyOf(output);
  }

  private void pushBinaryOperator(BinaryOperator current) {
    boolean checkNext = true;
    while (checkNext) {
      Operator operator = popBefore(current);
      if (operator != null) {
        output.add(operator);
      } else {
        checkNext = false;
      }
    }
    stack.addLast(current);
  }

  private void pushUnaryOperator(UnaryOperator current) {
    stack.addLast(current);
  }

  private @Nullable Operator popBefore(BinaryOperator current) {
    var top = stack.peekLast();
    return switch (top) {
      case UnaryOperator operator -> {
        stack.removeLast();
        yield operator;
      }
      case BinaryOperator operator when operator.precedence() >= current.precedence() -> {
        stack.removeLast();
        yield operator;
      }
      case null, default -> null;
    };
  }

  private void handleRightParenthesis(Parenthesis.Right rightParenthesis) {
    if (previous instanceof Parenthesis.Left(int positionInExpression)) {
      throw new InvalidArithmeticExpressionException(
          Messages.emptyParentheses(positionInExpression));
    }
    drainToLeftParenthesis(rightParenthesis);
  }

  private void drainToLeftParenthesis(Parenthesis.Right rightParenthesis) {
    while (!isLeftParenthesis()) {
      var popped = stack.pollLast();
      if (!(popped instanceof Operator operator)) {
        throw new InvalidArithmeticExpressionException(
            Messages.unmatched(')', rightParenthesis.positionInExpression()));
      }
      output.add(operator);
    }
    stack.removeLast();
  }

  private boolean isLeftParenthesis() {
    return stack.peekLast() instanceof Parenthesis.Left;
  }

  private void drainRemainingOperators() {
    while (!stack.isEmpty()) {
      var top = stack.removeLast();
      if (!(top instanceof Operator operator)) {
        throw new InvalidArithmeticExpressionException(
            Messages.unmatched('(', top.positionInExpression()));
      }
      output.add(operator);
    }
  }
}
