package example.calculator;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ActiveProfiles("calculator")
class CalculatorEndpointTest {

  @Autowired private RestTestClient restClient;

  @Test
  void shouldReturnCalculatedResultForValidExpression() {
    restClient
        .post()
        .uri("/calculate")
        .body("1+2*3")
        .exchange()
        .expectStatus()
        .isOk()
        .expectBody(String.class)
        .isEqualTo("7.0");
  }

  @Test
  void shouldReturnBadRequestForInvalidExpression() {
    restClient
        .post()
        .uri("/calculate")
        .body("abc")
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
        .isEqualTo(
            Map.of(
                "status", "400",
                "error", "Bad Request",
                "message", "Invalid character 'a' at position '0' in expression."));
  }
}
