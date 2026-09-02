package br.com.sgpj.service;

import br.com.sgpj.config.jwt.GerenciadorTokenJwt;
import br.com.sgpj.dto.response.RefreshTokenResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.UsuarioRefreshToken;
import br.com.sgpj.exception.TokenInvalidoException;
import br.com.sgpj.exception.UsuarioInativoException;
import br.com.sgpj.repository.UsuarioRefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private UsuarioRefreshTokenRepository usuarioRefreshTokenRepository;

    @Mock
    private GerenciadorTokenJwt gerenciadorTokenJwt;

    @Mock
    private AutenticacaoService autenticacaoService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final Integer REFRESH_TOKEN_VALIDITY = 86400; // 24 horas em segundos
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "REFRESH_TOKEN_VALIDITY", REFRESH_TOKEN_VALIDITY);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");
        usuario.setAtivo(true);
    }

    // =========================================================================
    // criarRefreshToken
    // =========================================================================

    @Nested
    @DisplayName("criarRefreshToken()")
    class CriarRefreshToken {

        @Test
        @DisplayName("Deve criar e salvar refresh token com hash SHA-256 e atributos válidos")
        void deveCriarESalvarRefreshToken() {
            // When
            String tokenGerado = refreshTokenService.criarRefreshToken(usuario);

            // Then
            assertNotNull(tokenGerado);
            assertFalse(tokenGerado.isBlank());

            ArgumentCaptor<UsuarioRefreshToken> captor = ArgumentCaptor.forClass(UsuarioRefreshToken.class);
            verify(usuarioRefreshTokenRepository, times(1)).save(captor.capture());

            UsuarioRefreshToken salvo = captor.getValue();
            assertNotNull(salvo.getToken());
            assertNotEquals(tokenGerado, salvo.getToken()); // O banco armazena o HASH, e não o token em texto claro
            assertEquals(64, salvo.getToken().length()); // Hash SHA-256 possui 64 caracteres hexadecimais
            assertEquals(usuario, salvo.getUsuario());
            assertFalse(salvo.getRevogado());
            assertNotNull(salvo.getCriadoEm());
            assertNotNull(salvo.getExpiraEm());
            assertTrue(salvo.getExpiraEm().isAfter(LocalDateTime.now()));
        }
    }

    // =========================================================================
    // validarERotacionar
    // =========================================================================

    @Nested
    @DisplayName("validarERotacionar()")
    class ValidarERotacionar {

        @Test
        @DisplayName("Deve rotacionar tokens com sucesso quando refresh token for válido e não expirado")
        void deveRotacionarTokensComSucesso() {
            // Given
            String tokenAntigo = "token-antigo-uuid";
            UsuarioRefreshToken refreshTokenExistente = UsuarioRefreshToken.builder()
                    .id(10L)
                    .token("hash-do-token-antigo")
                    .usuario(usuario)
                    .revogado(false)
                    .criadoEm(LocalDateTime.now().minusHours(1))
                    .expiraEm(LocalDateTime.now().plusHours(23))
                    .build();

            UserDetails userDetailsMock = mock(UserDetails.class);
            when(userDetailsMock.getAuthorities()).thenReturn(Collections.emptyList());

            when(usuarioRefreshTokenRepository.findByToken(anyString()))
                    .thenReturn(Optional.of(refreshTokenExistente));
            when(autenticacaoService.loadUserByUsername(usuario.getEmail())).thenReturn(userDetailsMock);
            when(gerenciadorTokenJwt.generateToken(any())).thenReturn("novo-access-token-jwt");

            // When
            RefreshTokenResponse response = refreshTokenService.validarERotacionar(tokenAntigo);

            // Then
            assertNotNull(response);
            assertEquals("novo-access-token-jwt", response.accessToken());
            assertNotNull(response.refreshToken());
            assertNotEquals(tokenAntigo, response.refreshToken());

            // Verifica que o token antigo foi revogado e salvo
            assertTrue(refreshTokenExistente.getRevogado());
            verify(usuarioRefreshTokenRepository, atLeast(2)).save(any(UsuarioRefreshToken.class));
            verify(autenticacaoService, times(1)).loadUserByUsername(usuario.getEmail());
            verify(gerenciadorTokenJwt, times(1)).generateToken(any());
        }

        @Test
        @DisplayName("Deve lançar TokenInvalidoException quando refresh token não for encontrado")
        void deveLancarExcecaoQuandoTokenNaoEncontrado() {
            // Given
            String tokenInexistente = "token-inexistente";
            when(usuarioRefreshTokenRepository.findByToken(anyString()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThrows(TokenInvalidoException.class, () -> refreshTokenService.validarERotacionar(tokenInexistente));

            verify(usuarioRefreshTokenRepository, never()).save(any());
            verify(gerenciadorTokenJwt, never()).generateToken(any());
        }

        @Test
        @DisplayName("Deve revogar todas as sessões e lançar exceção ao detectar reutilização de token já revogado")
        void deveRevogarSessoesAoDetectarReutilizacaoDeTokenRevogado() {
            // Given
            String tokenRevogado = "token-revogado-uuid";
            UsuarioRefreshToken refreshTokenRevogado = UsuarioRefreshToken.builder()
                    .id(15L)
                    .token("hash-revogado")
                    .usuario(usuario)
                    .revogado(true)
                    .criadoEm(LocalDateTime.now().minusHours(2))
                    .expiraEm(LocalDateTime.now().plusHours(22))
                    .build();

            when(usuarioRefreshTokenRepository.findByToken(anyString()))
                    .thenReturn(Optional.of(refreshTokenRevogado));

            // When & Then
            assertThrows(TokenInvalidoException.class, () -> refreshTokenService.validarERotacionar(tokenRevogado));

            verify(usuarioRefreshTokenRepository, times(1)).deleteByUsuario(usuario);
            verify(gerenciadorTokenJwt, never()).generateToken(any());
        }

        @Test
        @DisplayName("Deve lançar UsuarioInativoException quando o usuário associado ao token estiver inativo")
        void deveLancarExcecaoQuandoUsuarioEstiverInativo() {
            // Given
            usuario.setAtivo(false);
            String tokenValido = "token-usuario-inativo";
            UsuarioRefreshToken refreshToken = UsuarioRefreshToken.builder()
                    .id(16L)
                    .token("hash-token")
                    .usuario(usuario)
                    .revogado(false)
                    .criadoEm(LocalDateTime.now().minusHours(1))
                    .expiraEm(LocalDateTime.now().plusHours(23))
                    .build();

            when(usuarioRefreshTokenRepository.findByToken(anyString()))
                    .thenReturn(Optional.of(refreshToken));

            // When & Then
            assertThrows(UsuarioInativoException.class, () -> refreshTokenService.validarERotacionar(tokenValido));

            assertTrue(refreshToken.getRevogado());
            verify(gerenciadorTokenJwt, never()).generateToken(any());
        }

        @Test
        @DisplayName("Deve lançar TokenInvalidoException e revogar token quando já estiver expirado")
        void deveLancarExcecaoERevogarQuandoTokenExpirado() {
            // Given
            String tokenExpirado = "token-expirado-uuid";
            UsuarioRefreshToken refreshTokenExpirado = UsuarioRefreshToken.builder()
                    .id(20L)
                    .token("hash-expirado")
                    .usuario(usuario)
                    .revogado(false)
                    .criadoEm(LocalDateTime.now().minusDays(2))
                    .expiraEm(LocalDateTime.now().minusHours(1))
                    .build();

            when(usuarioRefreshTokenRepository.findByToken(anyString()))
                    .thenReturn(Optional.of(refreshTokenExpirado));

            // When & Then
            assertThrows(TokenInvalidoException.class, () -> refreshTokenService.validarERotacionar(tokenExpirado));

            assertTrue(refreshTokenExpirado.getRevogado());
            verify(usuarioRefreshTokenRepository, times(1)).save(refreshTokenExpirado);
            verify(gerenciadorTokenJwt, never()).generateToken(any());
        }
    }

    // =========================================================================
    // revogarToken
    // =========================================================================

    @Nested
    @DisplayName("revogarToken()")
    class RevogarToken {

        @Test
        @DisplayName("Deve marcar token como revogado e salvar quando encontrado")
        void deveRevogarTokenQuandoEncontrado() {
            // Given
            String tokenString = "token-para-revogar";
            UsuarioRefreshToken tokenExistente = UsuarioRefreshToken.builder()
                    .id(30L)
                    .token("hash-para-revogar")
                    .usuario(usuario)
                    .revogado(false)
                    .criadoEm(LocalDateTime.now())
                    .expiraEm(LocalDateTime.now().plusDays(1))
                    .build();

            when(usuarioRefreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(tokenExistente));

            // When
            refreshTokenService.revogarToken(tokenString);

            // Then
            assertTrue(tokenExistente.getRevogado());
            verify(usuarioRefreshTokenRepository, times(1)).save(tokenExistente);
        }

        @Test
        @DisplayName("Não deve lançar erro nem salvar quando token não for encontrado para revogar")
        void deveNaoFazerNadaQuandoTokenNaoEncontrado() {
            // Given
            String tokenString = "token-nao-encontrado";
            when(usuarioRefreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            // When
            refreshTokenService.revogarToken(tokenString);

            // Then
            verify(usuarioRefreshTokenRepository, never()).save(any());
        }
    }
}
