package br.com.sgpj.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioNaoEncontrado(UsuarioNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CargoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleCargoNaoEncontrado(CargoNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNaoEncontrado(ClienteNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClienteJaExistenteException.class)
    public ResponseEntity<Map<String, Object>> handleClienteJaExistente(ClienteJaExistenteException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleEmailDuplicado(EmailDuplicadoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ObjetoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleObjetoNaoEncontrado(ObjetoNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CaminhoJaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleCaminhoJaExiste(CaminhoJaExisteException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OneDriveErroException.class)
    public ResponseEntity<Map<String, Object>> handleOneDriveError(OneDriveErroException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage()
                                : "Valor inválido",
                        (a, b) -> a
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PasswordConflictException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordConflictException(PasswordConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(LimiteTentativasExcedidoException.class)
    public ResponseEntity<Map<String, Object>> handlePasswordConflictException(LimiteTentativasExcedidoException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(EscopoJwtException.class)
    public ResponseEntity<Map<String, Object>> handleEscopoJwtException(EscopoJwtException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                        },
                        v -> v.getMessage() != null ? v.getMessage() : "Valor inválido",
                        (a, b) -> a
                ));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        String mensagem = String.format("O parâmetro '%s' é obrigatório e não foi informado.", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String mensagem = String.format("O parâmetro '%s' possui valor inválido: '%s'. Tipo esperado: %s.",
                ex.getName(), ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconhecido");
        return buildResponse(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
