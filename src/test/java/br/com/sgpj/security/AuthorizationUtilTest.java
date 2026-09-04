package br.com.sgpj.security;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
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
@DisplayName("AuthorizationUtil - Testes")
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

    @BeforeEach
    void setUp() {
        authorizationUtil = new AuthorizationUtil(
                usuarioAutenticacaoAdapter,
                usuarioRepository,
                clienteRepository,
                processoAppRepository
        );
    }

    @Nested
    @DisplayName("getUsuarioLogado")
    class GetUsuarioLogadoTest {

        @Test
        @DisplayName("Deve retornar usuário logado quando encontrado")
        void deveRetornarUsuarioLogado() {
            String email = "usuario@teste.com";
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            usuario.setNome("Usuário Teste");

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));

            Usuario resultado = authorizationUtil.getUsuarioLogado();

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            assertEquals(email, resultado.getEmail());
            verify(usuarioAutenticacaoAdapter).obterEmailUsuarioLogado();
            verify(usuarioRepository).findByEmail(email);
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não for encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
            String email = "naoexiste@teste.com";

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());

            assertThrows(AcessoNegadoException.class, () -> authorizationUtil.getUsuarioLogado());
            
            verify(usuarioAutenticacaoAdapter).obterEmailUsuarioLogado();
            verify(usuarioRepository).findByEmail(email);
        }
    }

    @Nested
    @DisplayName("verificarAcessoCliente")
    class VerificarAcessoClienteTest {

        @Test
        @DisplayName("Deve permitir acesso quando cliente existe (atualmente permite todos)")
        void devePermitirAcessoCliente() {
            Long clienteId = 1L;
            String email = "usuario@teste.com";
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            
            Cliente cliente = new Cliente();
            cliente.setId(clienteId);
            cliente.setNomeRazaoSocial("Cliente Teste");

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

            assertDoesNotThrow(() -> authorizationUtil.verificarAcessoCliente(clienteId));
            
            verify(usuarioAutenticacaoAdapter).obterEmailUsuarioLogado();
            verify(usuarioRepository).findByEmail(email);
            verify(clienteRepository).findById(clienteId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void deveLancarExcecaoQuandoClienteNaoExiste() {
            Long clienteId = 999L;
            String email = "usuario@teste.com";
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            assertThrows(AcessoNegadoException.class, () -> authorizationUtil.verificarAcessoCliente(clienteId));
            
            verify(clienteRepository).findById(clienteId);
        }
    }

    @Nested
    @DisplayName("verificarAcessoProcesso")
    class VerificarAcessoProcessoTest {

        @Test
        @DisplayName("Deve permitir acesso quando usuário é responsável pelo processo")
        void devePermitirAcessoQuandoUsuarioResponsavel() {
            Long processoId = 1L;
            String email = "responsavel@teste.com";
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            
            ProcessoApp processo = new ProcessoApp();
            processo.setId(processoId);
            processo.setUsuarioResponsavel(usuario);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
            when(processoAppRepository.findById(processoId)).thenReturn(Optional.of(processo));

            assertDoesNotThrow(() -> authorizationUtil.verificarAcessoProcesso(processoId));
            
            verify(usuarioAutenticacaoAdapter).obterEmailUsuarioLogado();
            verify(usuarioRepository).findByEmail(email);
            verify(processoAppRepository).findById(processoId);
        }

        @Test
        @DisplayName("Deve negar acesso quando usuário não é responsável pelo processo")
        void deveNegarAcessoQuandoUsuarioNaoResponsavel() {
            Long processoId = 1L;
            String email = "outro@teste.com";
            Usuario usuarioLogado = new Usuario();
            usuarioLogado.setId(2L);
            usuarioLogado.setEmail(email);
            
            Usuario usuarioResponsavel = new Usuario();
            usuarioResponsavel.setId(1L);
            usuarioResponsavel.setEmail("responsavel@teste.com");
            
            ProcessoApp processo = new ProcessoApp();
            processo.setId(processoId);
            processo.setUsuarioResponsavel(usuarioResponsavel);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioLogado));
            when(processoAppRepository.findById(processoId)).thenReturn(Optional.of(processo));

            assertThrows(AcessoNegadoException.class, () -> authorizationUtil.verificarAcessoProcesso(processoId));
            
            verify(processoAppRepository).findById(processoId);
        }

        @Test
        @DisplayName("Deve negar acesso quando processo não tem responsável")
        void deveNegarAcessoQuandoProcessoSemResponsavel() {
            Long processoId = 1L;
            String email = "usuario@teste.com";
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);
            
            ProcessoApp processo = new ProcessoApp();
            processo.setId(processoId);
            processo.setUsuarioResponsavel(null);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
            when(processoAppRepository.findById(processoId)).thenReturn(Optional.of(processo));

            assertThrows(AcessoNegadoException.class, () -> authorizationUtil.verificarAcessoProcesso(processoId));
        }

        @Test
        @DisplayName("Deve lançar exceção quando processo não existe")
        void deveLancarExcecaoQuandoProcessoNaoExiste() {
            Long processoId = 999L;
            String email = "usuario@teste.com";
            Usuario usuario = new Usuario();
            usuario.setId(1L);
            usuario.setEmail(email);

            when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn(email);
            when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuario));
            when(processoAppRepository.findById(processoId)).thenReturn(Optional.empty());

            assertThrows(AcessoNegadoException.class, () -> authorizationUtil.verificarAcessoProcesso(processoId));
            
            verify(processoAppRepository).findById(processoId);
        }
    }
}
