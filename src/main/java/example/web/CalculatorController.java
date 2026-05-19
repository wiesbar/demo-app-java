package example.web;

import example.calculator.ArithmeticExpressionCalculator;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("calculator")
public class CalculatorController {

  private final ArithmeticExpressionCalculator calculator;

  CalculatorController(ArithmeticExpressionCalculator calculator) {
    this.calculator = calculator;
  }

  @PostMapping("/calculate")
  public String calculate(@RequestBody String expression) {
    return Double.toString(calculator.calculate(expression));
  }
}
