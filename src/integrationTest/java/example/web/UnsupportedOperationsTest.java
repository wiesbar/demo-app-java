package example.web;

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
class UnsupportedOperationsTest {

  @Autowired private RestTestClient restClient;

  @Test
  void shouldReturnExpectedErrorForUnsupportedRequest() {
    restClient
        .get()
        .uri("/calculate")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .expectBody(new ParameterizedTypeReference<Map<String, String>>() {})
        .isEqualTo(
            Map.of(
                "status", "500",
                "error", "Internal Server Error",
                "message", "Internal Server Error"));
  }
}
