package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioAtualizarPerfilRequest;
import br.com.sgpj.dto.response.Usuario.UsuarioListarResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarMeuPerfil - Atualização de dados pessoais pelo próprio usuário")
class AtualizarMeuPerfilTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    @InjectMocks
    private AtualizarMeuPerfil atualizarMeuPerfil;

    @Test
    @DisplayName("Deve atualizar nome e e-mail mantendo cargo e status inalterados")
    void deveAtualizarNomeEEmailDoProprioPerfil() {
        Cargo cargoAdvogado = new Cargo(3L, "Advogado", null, LocalDateTime.now());
        Usuario logado = new Usuario(15L, "Nome Antigo", "antigo@sgpj.com", "hash", cargoAdvogado, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("antigo@sgpj.com");
        when(usuarioRepository.findByEmail("antigo@sgpj.com")).thenReturn(logado);
        when(usuarioRepository.existsByEmail("novo@sgpj.com")).thenReturn(false);

        UsuarioAtualizarPerfilRequest req = new UsuarioAtualizarPerfilRequest("  Nome Novo  ", "  novo@sgpj.com  ");

        UsuarioListarResponse resp = atualizarMeuPerfil.executar(req);

        assertThat(resp.nome()).isEqualTo("Nome Novo");
        assertThat(resp.email()).isEqualTo("novo@sgpj.com");
        assertThat(resp.cargoId()).isEqualTo(3L);
        assertThat(resp.ativo()).isTrue();
        verify(usuarioRepository).save(logado);
    }

    @Test
    @DisplayName("Deve lançar EmailDuplicadoException quando novo e-mail já estiver em uso")
    void naoDevePermitirEmailJaExistente() {
        Cargo cargoAdvogado = new Cargo(3L, "Advogado", null, LocalDateTime.now());
        Usuario logado = new Usuario(15L, "Nome", "meu@sgpj.com", "hash", cargoAdvogado, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("meu@sgpj.com");
        when(usuarioRepository.findByEmail("meu@sgpj.com")).thenReturn(logado);
        when(usuarioRepository.existsByEmail("outro@sgpj.com")).thenReturn(true);

        UsuarioAtualizarPerfilRequest req = new UsuarioAtualizarPerfilRequest("Nome", "outro@sgpj.com");

        assertThatThrownBy(() -> atualizarMeuPerfil.executar(req))
                .isInstanceOf(EmailDuplicadoException.class);
    }
}
