package br.com.sgpj.security;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.AcessoNegadoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AuthorizationUtil")
class AuthorizationUtilTest {

    @Mock
    private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ProcessoAppRepository processoAppRepository;

    private AuthorizationUtil authorizationUtil;

    private Usuario usuarioLogado;

    @BeforeEach
    void setUp() {
        authorizationUtil = new AuthorizationUtil(
                usuarioAutenticacaoAdapter,
                usuarioRepository,
                clienteRepository,
                processoAppRepository
        );

        usuarioLogado = new Usuario(1L, "logado@sgpj.com.br", "senha");
    }

    @Nested
    @DisplayName("getUsuarioLogado")
    class GetUsuarioLogadoTest {

        @Test
        @DisplayName("Deve retornar o usuário quando o email do token existir no repositório")
        void deveRetornarUsuarioLogado() {
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);

            Usuario resultado = authorizationUtil.getUsuarioLogado();

            assertEquals(usuarioLogado, resultado);
        }

        @Test
        @DisplayName("Deve lançar AcessoNegadoException quando o usuário não for encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("inexistente@sgpj.com.br");
            when(usuarioRepository.findByEmail("inexistente@sgpj.com.br")).thenReturn(null);

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> authorizationUtil.getUsuarioLogado());
            assertEquals("Usuário não encontrado", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("verificarAcessoCliente")
    class VerificarAcessoClienteTest {

        @Test
        @DisplayName("Não deve lançar exceção quando o cliente existir (acesso sempre liberado)")
        void naoDeveLancarExcecaoQuandoClienteExiste() {
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);
            when(clienteRepository.findById(10L)).thenReturn(Optional.of(mock(Cliente.class)));

            assertDoesNotThrow(() -> authorizationUtil.verificarAcessoCliente(10L));
        }

        @Test
        @DisplayName("Deve lançar AcessoNegadoException quando o cliente não existir")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> authorizationUtil.verificarAcessoCliente(99L));
            assertEquals("Cliente não encontrado", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("verificarAcessoProcesso")
    class VerificarAcessoProcessoTest {

        @Test
        @DisplayName("Não deve lançar exceção quando o usuário logado for o responsável pelo processo")
        void naoDeveLancarExcecaoQuandoUsuarioEhResponsavel() {
            ProcessoApp processo = mock(ProcessoApp.class);
            when(processo.getUsuarioResponsavel()).thenReturn(usuarioLogado);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);
            when(processoAppRepository.findById(5L)).thenReturn(Optional.of(processo));

            assertDoesNotThrow(() -> authorizationUtil.verificarAcessoProcesso(5L));
        }

        @Test
        @DisplayName("Deve lançar AcessoNegadoException quando o usuário logado não for o responsável")
        void deveLancarExcecaoQuandoUsuarioNaoEhResponsavel() {
            Usuario outroUsuario = new Usuario(2L, "outro@sgpj.com.br", "senha");
            ProcessoApp processo = mock(ProcessoApp.class);
            when(processo.getUsuarioResponsavel()).thenReturn(outroUsuario);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);
            when(processoAppRepository.findById(5L)).thenReturn(Optional.of(processo));

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> authorizationUtil.verificarAcessoProcesso(5L));
            assertEquals("Acesso negado ao processo", ex.getMessage());
        }

        @Test
        @DisplayName("Deve permitir processo ativo sem responsável para registros legados")
        void devePermitirProcessoAtivoSemResponsavel() {
            ProcessoApp processo = mock(ProcessoApp.class);
            when(processo.getUsuarioResponsavel()).thenReturn(null);
            when(processo.getStatus()).thenReturn(StatusProcesso.ATIVO);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);
            when(processoAppRepository.findById(7L)).thenReturn(Optional.of(processo));

            assertDoesNotThrow(() -> authorizationUtil.verificarAcessoProcesso(7L));
        }

        @Test
        @DisplayName("Deve negar acesso a solicitação pendente sem responsável")
        void deveNegarSolicitacaoSemResponsavel() {
            ProcessoApp processo = mock(ProcessoApp.class);
            when(processo.getUsuarioResponsavel()).thenReturn(null);
            when(processo.getStatus()).thenReturn(StatusProcesso.SOLICITACAO);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);
            when(processoAppRepository.findById(7L)).thenReturn(Optional.of(processo));

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> authorizationUtil.verificarAcessoProcesso(7L));
            assertEquals("Acesso negado ao processo", ex.getMessage());
        }

        @Test
        @DisplayName("Deve lançar AcessoNegadoException quando o processo não for encontrado")
        void deveLancarExcecaoQuandoProcessoNaoEncontrado() {
            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("logado@sgpj.com.br");
            when(usuarioRepository.findByEmail("logado@sgpj.com.br")).thenReturn(usuarioLogado);
            when(processoAppRepository.findById(123L)).thenReturn(Optional.empty());

            AcessoNegadoException ex = assertThrows(AcessoNegadoException.class,
                    () -> authorizationUtil.verificarAcessoProcesso(123L));
            assertEquals("Processo não encontrado", ex.getMessage());
        }
    }
}
