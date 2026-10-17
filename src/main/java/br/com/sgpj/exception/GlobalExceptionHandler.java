package br.com.sgpj.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
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
        return buildResponse(HttpStatus.NOT_FOUND, "Usuário não encontrado");
    }

    @ExceptionHandler(CargoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleCargoNaoEncontrado(CargoNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNaoEncontrado(ClienteNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(StatusClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleStatusClienteNaoEncontrado(StatusClienteNaoEncontradoException ex) {
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
        return buildResponse(HttpStatus.UNAUTHORIZED, "E-mail e/ou senha inválido(s)");
    }

    @ExceptionHandler(OtpInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleOtpInvalida(OtpInvalidaException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Usuário não encontrado");
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

    @ExceptionHandler(OneDriveNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleOneDriveNotFound(OneDriveNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
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

        Map<String, Object> body = buildValidationResponse(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CaptchaInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleCaptchaInvalido(CaptchaInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
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

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleTokenInvalidoException(TokenInvalidoException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UsuarioJaAtivoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioJaAtivoException(UsuarioJaAtivoException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UsuarioInativoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioInativoException(UsuarioInativoException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UsuarioAutoExclusaoException.class)
    public ResponseEntity<Map<String, Object>> handleUsuarioAutoExclusaoException(UsuarioAutoExclusaoException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(PermissaoNaoEncontradaException.class)
    public ResponseEntity<Map<String, Object>> handlePermissaoNaoEncontradaException(PermissaoNaoEncontradaException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OperacaoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleOperacaoInvalidaException(OperacaoInvalidaException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CargoJaExisteException.class)
    public ResponseEntity<Map<String, Object>> handleCargoJaExisteException(CargoJaExisteException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<Map<String, Object>> handleEmailSendException(EmailSendException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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

        Map<String, Object> body = buildValidationResponse(fieldErrors);
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

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyReference(PropertyReferenceException ex) {
        String mensagem = String.format("Campo de ordenação inválido: '%s'. Verifique o nome do campo informado no parâmetro 'sort'.", ex.getPropertyName());
        return buildResponse(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRateLimitExceeded(RateLimitExceededException ex) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(OtpGenerationLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleOtpGenerationLimitExceeded(OtpGenerationLimitExceededException ex) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(TokenBlacklistedException.class)
    public ResponseEntity<Map<String, Object>> handleTokenBlacklisted(TokenBlacklistedException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ProcessoJaExistenteException.class)
    public ResponseEntity<Map<String, Object>> handleProcessoJaExistente(ProcessoJaExistenteException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataJudIntegrationException.class)
    public ResponseEntity<Map<String, Object>> handleDataJudIntegration(DataJudIntegrationException ex) {
        String msg = (ex.getMessage() != null && !ex.getMessage().isBlank())
                ? ex.getMessage()
                : "Não foi possível obter dados oficiais do DataJud no momento. Por favor, tente novamente mais tarde.";
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, msg);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String msg = "Formato de dados inválido na requisição.";
        if (ex.getMessage() != null && ex.getMessage().contains("LocalDateTime")) {
            msg = "Data ou horário em formato inválido. Verifique o ano, mês, dia e horário informados.";
        }
        return buildResponse(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        String msg = ex.getMessage() != null && !ex.getMessage().isBlank() ? ex.getMessage() : "Erro interno inesperado no servidor.";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, msg);
    }

    @ExceptionHandler(TribunalInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleTribunalInvalido(TribunalInvalidoException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> buildValidationResponse(Map<String, String> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        String primeiraMensagem = fieldErrors.values().stream().findFirst().orElse("Dados inválidos na requisição.");
        body.put("message", primeiraMensagem);
        body.put("errors", fieldErrors);
        return body;
    }
}
