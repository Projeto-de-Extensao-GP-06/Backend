package br.com.sgpj.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IpAddressUtil - Testes de extração de IP")
class IpAddressUtilTest {

    @Mock
    private HttpServletRequest request;

    @Nested
    @DisplayName("getClientIpAddress - Testes de extração de IP")
    class GetClientIpAddressTest {

        @Test
        @DisplayName("Deve extrair IP de X-Forwarded-For")
        void deveExtrairIpDeXForwardedFor() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

            String ip = IpAddressUtil.getClientIpAddress(request);
            assertEquals("192.168.1.100", ip);
        }

        @Test
        @DisplayName("Deve extrair primeiro IP quando múltiplos em X-Forwarded-For")
        void deveExtrairPrimeiroIpQuandoMultiplosEmXForwardedFor() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100, 10.0.0.1, 172.16.0.1");

            String ip = IpAddressUtil.getClientIpAddress(request);
            assertEquals("192.168.1.100", ip);
        }

        @Test
        @DisplayName("Deve usar Proxy-Client-IP quando X-Forwarded-For está vazio")
        void deveUsarProxyClientIpQuandoXForwardedForVazio() {
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn("192.168.1.101");

            String ip = IpAddressUtil.getClientIpAddress(request);
            assertEquals("192.168.1.101", ip);
        }

        @Test
        @DisplayName("Deve usar remoteAddr quando nenhum header está presente")
        void deveUsarRemoteAddrQuandoNenhumHeaderPresente() {
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
            when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");

            String ip = IpAddressUtil.getClientIpAddress(request);
            assertEquals("10.0.0.1", ip);
        }

        @Test
        @DisplayName("Deve retornar 'unknown' quando não é possível determinar o IP")
        void deveRetornarUnknownQuandoNaoPossivelDeterminarIp() {
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn(null);
            when(request.getHeader("HTTP_CLIENT_IP")).thenReturn(null);
            when(request.getHeader("HTTP_X_FORWARDED_FOR")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn(null);

            String ip = IpAddressUtil.getClientIpAddress(request);
            assertEquals("unknown", ip);
        }

        @Test
        @DisplayName("Deve ignorar valores 'unknown' nos headers")
        void deveIgnorarValoresUnknownNosHeaders() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
            when(request.getHeader("Proxy-Client-IP")).thenReturn("unknown");
            when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("192.168.1.102");

            String ip = IpAddressUtil.getClientIpAddress(request);
            assertEquals("192.168.1.102", ip);
        }
    }

    @Nested
    @DisplayName("createRateLimitIdentifier - Testes de criação de identificador")
    class CreateRateLimitIdentifierTest {

        @Test
        @DisplayName("Deve combinar IP e email quando ambos fornecidos")
        void deveCombinarIpEEmailQuandoAmbosFornecidos() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

            String identifier = IpAddressUtil.createRateLimitIdentifier(request, "user@example.com");
            assertEquals("192.168.1.100:user@example.com", identifier);
        }

        @Test
        @DisplayName("Deve normalizar email para minúsculas")
        void deveNormalizarEmailParaMinusculas() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

            String identifier = IpAddressUtil.createRateLimitIdentifier(request, "USER@EXAMPLE.COM");
            assertEquals("192.168.1.100:user@example.com", identifier);
        }

        @Test
        @DisplayName("Deve usar apenas IP quando email é nulo")
        void deveUsarApenasIpQuandoEmailNulo() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

            String identifier = IpAddressUtil.createRateLimitIdentifier(request, null);
            assertEquals("192.168.1.100", identifier);
        }

        @Test
        @DisplayName("Deve usar apenas IP quando email está em branco")
        void deveUsarApenasIpQuandoEmailEmBranco() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

            String identifier = IpAddressUtil.createRateLimitIdentifier(request, "   ");
            assertEquals("192.168.1.100", identifier);
        }

        @Test
        @DisplayName("Deve remover espaços do email")
        void deveRemoverEspacosDoEmail() {
            when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

            String identifier = IpAddressUtil.createRateLimitIdentifier(request, "  user@example.com  ");
            assertEquals("192.168.1.100:user@example.com", identifier);
        }
    }
}