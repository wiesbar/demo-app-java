package example.web;

import example.calculator.InvalidArithmeticExpressionException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private static final String GENERIC_ERROR_MESSAGE = "Internal Server Error";

  @ExceptionHandler(InvalidArithmeticExpressionException.class)
  ResponseEntity<Map<String, String>> handleInvalidExpression(
      InvalidArithmeticExpressionException ex) {
    return badRequest(Objects.requireNonNull(ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException ex) {
    var mostSpecific = ex.getMostSpecificCause().getMessage();
    return badRequest(
        mostSpecific != null
            ? mostSpecific
            : Objects.requireNonNullElse(ex.getMessage(), "Unreadable request body"));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Map<String, String>> handleGeneralError(Exception ex) {
    LOGGER.error("Unhandled exception reached GlobalExceptionHandler", ex);
    return errorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR, GENERIC_ERROR_MESSAGE, GENERIC_ERROR_MESSAGE);
  }

  private static ResponseEntity<Map<String, String>> badRequest(String message) {
    return errorResponse(HttpStatus.BAD_REQUEST, "Bad Request", message);
  }

  private static ResponseEntity<Map<String, String>> errorResponse(
      HttpStatus status, String error, String message) {
    var body = new LinkedHashMap<String, String>();
    body.put("status", String.valueOf(status.value()));
    body.put("error", error);
    body.put("message", message);
    return ResponseEntity.status(status).body(body);
  }
}
