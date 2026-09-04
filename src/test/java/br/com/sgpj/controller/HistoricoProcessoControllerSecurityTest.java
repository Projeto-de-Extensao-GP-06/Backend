package br.com.sgpj.controller;

import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.AcessoNegadoException;
import br.com.sgpj.security.AuthorizationUtil;
import br.com.sgpj.service.HistoricoProcessoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HistoricoProcessoController - Security Tests")
class HistoricoProcessoControllerSecurityTest {

    @Mock
    private HistoricoProcessoService historicoProcessoService;

    @Mock
    private AuthorizationUtil authorizationUtil;

    @Mock
    private HistoricoProcessoRequest historicoProcessoRequest;

    @Mock
    private HistoricoProcessoResponse historicoProcessoResponse;

    @Mock
    private Usuario usuario;

    @InjectMocks
    private HistoricoProcessoController historicoProcessoController;

    @Nested
    @DisplayName("POST /historico - interacaoProcesso")
    class InteracaoProcessoSecurityTest {

        @Test
        @DisplayName("Deve obter usuario autenticado antes de registrar historico")
        void deveObterUsuarioAutenticadoAntesDeRegistrarHistorico() {
            when(authorizationUtil.getUsuarioLogado()).thenReturn(usuario);
            when(historicoProcessoService.adicionarRegistro(historicoProcessoRequest, usuario)).thenReturn(historicoProcessoResponse);

            ResponseEntity<HistoricoProcessoResponse> resposta = historicoProcessoController.interacaoProcesso(historicoProcessoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isEqualTo(historicoProcessoResponse);

            verify(authorizationUtil).getUsuarioLogado();
            verify(historicoProcessoService).adicionarRegistro(historicoProcessoRequest, usuario);
        }

        @Test
        @DisplayName("Deve propagar excecao quando usuario nao for encontrado")
        void devePropagarExcecaoQuandoUsuarioNaoForEncontrado() {
            when(authorizationUtil.getUsuarioLogado()).thenThrow(new AcessoNegadoException("Usuário não encontrado"));

            assertThatThrownBy(() -> historicoProcessoController.interacaoProcesso(historicoProcessoRequest))
                    .isInstanceOf(AcessoNegadoException.class)
                    .hasMessage("Usuário não encontrado");

            verify(authorizationUtil).getUsuarioLogado();
            verifyNoInteractions(historicoProcessoService);
        }

        @Test
        @DisplayName("Deve propagar excecao quando acesso for negado")
        void devePropagarExcecaoQuandoAcessoForNegado() {
            when(authorizationUtil.getUsuarioLogado()).thenThrow(new AcessoNegadoException("Acesso negado"));

            assertThatThrownBy(() -> historicoProcessoController.interacaoProcesso(historicoProcessoRequest))
                    .isInstanceOf(AcessoNegadoException.class)
                    .hasMessage("Acesso negado");

            verify(authorizationUtil).getUsuarioLogado();
            verifyNoInteractions(historicoProcessoService);
        }

        @Test
        @DisplayName("Deve garantir que usuario autenticado e sempre usado no historico")
        void deveGarantirQueUsuarioAutenticadoESempreUsadoNoHistorico() {
            when(authorizationUtil.getUsuarioLogado()).thenReturn(usuario);
            when(historicoProcessoService.adicionarRegistro(historicoProcessoRequest, usuario)).thenReturn(historicoProcessoResponse);

            historicoProcessoController.interacaoProcesso(historicoProcessoRequest);

            verify(authorizationUtil, times(1)).getUsuarioLogado();
            verify(historicoProcessoService, times(1)).adicionarRegistro(historicoProcessoRequest, usuario);
        }

        @Test
        @DisplayName("Deve impedir que usuario falsificado seja usado")
        void deveImpedirQueUsuarioFalsificadoSejaUsado() {
            Usuario usuarioAutenticado = mock(Usuario.class);
            Usuario usuarioFalsificado = mock(Usuario.class);
            
            when(authorizationUtil.getUsuarioLogado()).thenReturn(usuarioAutenticado);
            when(historicoProcessoService.adicionarRegistro(historicoProcessoRequest, usuarioAutenticado)).thenReturn(historicoProcessoResponse);

            ResponseEntity<HistoricoProcessoResponse> resposta = historicoProcessoController.interacaoProcesso(historicoProcessoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(authorizationUtil).getUsuarioLogado();
            verify(historicoProcessoService).adicionarRegistro(historicoProcessoRequest, usuarioAutenticado);
            
            // Verify that the service was called exactly once with the authenticated user
            verify(historicoProcessoService, times(1)).adicionarRegistro(any(), any());
        }
    }
}
