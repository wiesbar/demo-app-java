package example.web;

import static org.assertj.core.api.Assertions.assertThat;

import example.calculator.InvalidArithmeticExpressionException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleGeneralErrorReturns500WithStaticMessageThatDoesNotLeakTheCause() {
    ResponseEntity<Map<String, String>> response =
        handler.handleGeneralError(new RuntimeException("contains secret=abc"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(response.getBody())
        .isNotNull()
        .hasSize(3)
        .containsAllEntriesOf(
            Map.of(
                "status", "500",
                "error", "Internal Server Error",
                "message", "Internal Server Error"));
  }

  @Test
  void handleGeneralErrorDoesNotIncludeOriginalExceptionMessageInAnyField() {
    String secret = "secret=super-confidential";

    ResponseEntity<Map<String, String>> response =
        handler.handleGeneralError(new IllegalStateException(secret));

    assertThat(response.getBody())
        .isNotNull()
        .allSatisfy((key, value) -> assertThat(value).doesNotContain(secret));
  }

  @Test
  void handleInvalidExpressionReturns400WithTheExceptionMessage() {
    ResponseEntity<Map<String, String>> response =
        handler.handleInvalidExpression(
            new InvalidArithmeticExpressionException("Invalid empty expression."));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .isNotNull()
        .hasSize(3)
        .containsAllEntriesOf(
            Map.of(
                "status", "400",
                "error", "Bad Request",
                "message", "Invalid empty expression."));
  }

  @Test
  void handleUnreadableBodyUsesTheMostSpecificCauseMessageWhenPresent() {
    HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
    Mockito.when(ex.getMostSpecificCause())
        .thenReturn(new RuntimeException("Required request body is missing."));

    ResponseEntity<Map<String, String>> response = handler.handleUnreadableBody(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .isNotNull()
        .hasSize(3)
        .containsAllEntriesOf(
            Map.of(
                "status", "400",
                "error", "Bad Request",
                "message", "Required request body is missing."));
  }

  @Test
  void handleUnreadableBodyFallsBackToTheExceptionMessageWhenMostSpecificCauseMessageIsNull() {
    HttpMessageNotReadableException ex = Mockito.mock(HttpMessageNotReadableException.class);
    Mockito.when(ex.getMostSpecificCause()).thenReturn(new RuntimeException());
    Mockito.when(ex.getMessage()).thenReturn("fallback message");

    ResponseEntity<Map<String, String>> response = handler.handleUnreadableBody(ex);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody())
        .isNotNull()
        .hasSize(3)
        .containsAllEntriesOf(
            Map.of(
                "status", "400",
                "error", "Bad Request",
                "message", "fallback message"));
  }
}
