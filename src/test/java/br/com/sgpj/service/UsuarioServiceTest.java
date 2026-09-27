package br.com.sgpj.service;

import br.com.sgpj.dto.request.DefinirNovaSenhaRequest;
import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.request.UsuarioCriacaoRequest;
import br.com.sgpj.dto.request.UsuarioLoginRequest;
import br.com.sgpj.dto.request.ValidarOtpRequest;
import br.com.sgpj.dto.response.AutenticacaoUsuarioResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioListarResponse;
import br.com.sgpj.dto.response.Usuario.UsuarioTokenResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.service.useCases.usuario.AutenticarOtp;
import br.com.sgpj.service.useCases.usuario.AutenticarUsuario;
import br.com.sgpj.service.useCases.usuario.CreateUsuario;
import br.com.sgpj.service.useCases.usuario.DefinirNovaSenha;
import br.com.sgpj.service.useCases.usuario.DeleteUsuario;
import br.com.sgpj.service.useCases.usuario.EnviarDefinirSenhaOtp;
import br.com.sgpj.service.useCases.usuario.EnviarRedefinirSenhaOtp;
import br.com.sgpj.service.useCases.usuario.ReadUsuario;
import br.com.sgpj.service.useCases.usuario.ReativarUsuario;
import br.com.sgpj.service.useCases.usuario.UpdateUsuario;
import br.com.sgpj.service.useCases.usuario.ValidarOtp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - delegação para use-cases")
class UsuarioServiceTest {

    @Mock
    private CreateUsuario createUsuario;

    @Mock
    private ReadUsuario readUsuario;

    @Mock
    private UpdateUsuario updateUsuario;

    @Mock
    private DeleteUsuario deleteUsuario;

    @Mock
    private ReativarUsuario reativarUsuario;

    @Mock
    private AutenticarUsuario autenticarUsuario;

    @Mock
    private AutenticarOtp autenticarOtp;

    @Mock
    private EnviarRedefinirSenhaOtp enviarRedefinirSenhaOtp;

    @Mock
    private EnviarDefinirSenhaOtp enviarDefinirSenhaOtp;

    @Mock
    private ValidarOtp validarOtp;

    @Mock
    private DefinirNovaSenha definirNovaSenha;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("criar delega para CreateUsuario")
    void criarDelegaParaCreateUsuario() {
        UsuarioCriacaoRequest req = mock(UsuarioCriacaoRequest.class);

        usuarioService.criar(req);

        verify(createUsuario, times(1)).executar(req);
    }

    @Test
    @DisplayName("listarTodos delega para ReadUsuario")
    void listarTodosDelegaParaReadUsuario() {
        when(readUsuario.listarTodos()).thenReturn(List.of());

        List<UsuarioListarResponse> res = usuarioService.listarTodos();

        assertNotNull(res);
        verify(readUsuario, times(1)).listarTodos();
    }

    @Test
    @DisplayName("listarPorCargo delega para ReadUsuario")
    void listarPorCargoDelega() {
        when(readUsuario.listarPorCargo("ADMIN")).thenReturn(List.of());

        List<UsuarioListarResponse> res = usuarioService.listarPorCargo("ADMIN");

        assertNotNull(res);
        verify(readUsuario).listarPorCargo("ADMIN");
    }

    @Test
    @DisplayName("buscarPorNomeOuEmail delega para ReadUsuario")
    void buscarPorNomeOuEmailDelega() {
        when(readUsuario.buscarPorNomeOuEmail("q")).thenReturn(List.of());

        List<UsuarioListarResponse> res = usuarioService.buscarPorNomeOuEmail("q");

        assertNotNull(res);
        verify(readUsuario).buscarPorNomeOuEmail("q");
    }

    @Test
    @DisplayName("obterPorId delega para ReadUsuario")
    void obterPorIdDelega() {
        Usuario u = new Usuario();
        when(readUsuario.obterPorId(1L)).thenReturn(u);

        Usuario res = usuarioService.obterPorId(1L);

        assertEquals(u, res);
        verify(readUsuario).obterPorId(1L);
    }

    @Test
    @DisplayName("atualizarUsuario delega para UpdateUsuario e retorna resposta")
    void atualizarDelegaERetorna() {
        UsuarioAtualizacaoRequest req = mock(UsuarioAtualizacaoRequest.class);
        UsuarioListarResponse resp = mock(UsuarioListarResponse.class);
        when(updateUsuario.executar(req, 1L)).thenReturn(resp);

        UsuarioListarResponse res = usuarioService.atualizarUsuario(1L, req);

        assertEquals(resp, res);
        verify(updateUsuario).executar(req, 1L);
    }

    @Test
    @DisplayName("deletar delega para DeleteUsuario")
    void deletarDelega() {
        usuarioService.deletar(1L);

        verify(deleteUsuario).executar(1L);
    }

    @Test
    @DisplayName("autenticar delega para AutenticarUsuario e retorna token")
    void autenticarDelegaERetornaToken() {
        UsuarioLoginRequest req = mock(UsuarioLoginRequest.class);
        AutenticacaoUsuarioResponse auth = mock(AutenticacaoUsuarioResponse.class);
        UsuarioTokenResponse tokenRes = mock(UsuarioTokenResponse.class);
        when(autenticarUsuario.executar(req)).thenReturn(auth);
        when(auth.tokenResponse()).thenReturn(tokenRes);

        UsuarioTokenResponse res = usuarioService.autenticar(req);

        assertEquals(tokenRes, res);
        verify(autenticarUsuario).executar(req);
    }

    @Test
    @DisplayName("autenticarOtp delega para AutenticarOtp e retorna token")
    void autenticarOtpDelega() {
        ValidarOtpRequest req = new ValidarOtpRequest("a", "1");
        AutenticacaoUsuarioResponse auth = mock(AutenticacaoUsuarioResponse.class);
        UsuarioTokenResponse tokenRes = mock(UsuarioTokenResponse.class);
        when(autenticarOtp.executar(req)).thenReturn(auth);
        when(auth.tokenResponse()).thenReturn(tokenRes);

        UsuarioTokenResponse res = usuarioService.autenticarOtp(req);

        assertEquals(tokenRes, res);
        verify(autenticarOtp).executar(req);
    }

    @Test
    @DisplayName("enviarRedefinirSenhaOtp delega para EnviarRedefinirSenhaOtp")
    void enviarRedefinirSenhaOtpDelega() {
        usuarioService.enviarRedefinirSenhaOtp("x@x.com");

        verify(enviarRedefinirSenhaOtp).executar("x@x.com");
    }

    @Test
    @DisplayName("enviarDefinirSenhaOtp delega para EnviarDefinirSenhaOtp")
    void enviarDefinirSenhaOtpDelega() {
        usuarioService.enviarDefinirSenhaOtp("x@x.com");

        verify(enviarDefinirSenhaOtp).executar("x@x.com");
    }

    @Test
    @DisplayName("validarOtp delega para ValidarOtp e retorna boolean")
    void validarOtpDelega() {
        ValidarOtpRequest req = new ValidarOtpRequest("a", "1");
        when(validarOtp.executar(req)).thenReturn(true);

        boolean res = usuarioService.validarOtp(req);

        assertTrue(res);
        verify(validarOtp).executar(req);
    }

    @Test
    @DisplayName("definirNovaSenha delega para DefinirNovaSenha")
    void definirNovaSenhaDelega() {
        DefinirNovaSenhaRequest req = mock(DefinirNovaSenhaRequest.class);

        usuarioService.definirNovaSenha(req);

        verify(definirNovaSenha).executar(req);
    }

    @Test
    @DisplayName("reativarUsuario delega e retorna resposta")
    void reativarDelegacao() {
        UsuarioListarResponse resp = mock(UsuarioListarResponse.class);
        when(reativarUsuario.executar(2L)).thenReturn(resp);

        UsuarioListarResponse res = usuarioService.reativarUsuario(2L);

        assertEquals(resp, res);
        verify(reativarUsuario).executar(2L);
    }
}