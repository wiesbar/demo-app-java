package example.config;

import example.calculator.ArithmeticExpressionCalculator;
import example.calculator.DefaultArithmeticExpressionCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("calculator")
class CalculatorConfig {

  @Bean
  ArithmeticExpressionCalculator calculator() {
    return new DefaultArithmeticExpressionCalculator();
  }
}
