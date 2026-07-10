package be.parcial.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private MockHttpServletRequest createRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        return request;
    }

    @Test
    @DisplayName("should handle ResourceNotFoundException")
    void handleResourceNotFound_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleResourceNotFound(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("should handle BadCredentialsException")
    void handleBadCredentials_returns401() {
        BadCredentialsException ex = new BadCredentialsException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleBadCredentials(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo(Messages.INVALID_CREDENTIALS);
    }

    @Test
    @DisplayName("should handle IllegalArgumentException")
    void handleIllegalArgument_returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleIllegalArgument(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid argument");
    }

    @Test
    @DisplayName("should handle SlotNoDisponibleException")
    void handleSlotNoDisponible_returns409() {
        SlotNoDisponibleException ex = new SlotNoDisponibleException("Ocupado");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleSlotNoDisponible(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Ocupado");
    }

    @Test
    @DisplayName("should handle AccessDeniedException")
    void handleAccessDenied_returns403() {
        org.springframework.security.access.AccessDeniedException ex =
                new org.springframework.security.access.AccessDeniedException("Denied");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleAccessDenied(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
    }

    @Test
    @DisplayName("should handle TransicionInvalidaException")
    void handleTransicionInvalida_returns400() {
        TransicionInvalidaException ex = new TransicionInvalidaException("PENDIENTE", "completar");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleTransicionInvalida(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("completar");
    }

    @Test
    @DisplayName("should handle generic Exception")
    void handleGenericException_returns500() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleGenericException(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("Unexpected error");
    }

    @Test
    @DisplayName("should handle MethodArgumentNotValidException")
    void handleValidationErrors_returns400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("requestDTO", "name", "Name cannot be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<ErrorResponse> response = globalExceptionHandler
                .handleValidationErrors(ex, createRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
        assertThat(response.getBody().getDetails()).isNotNull();
    }
}
