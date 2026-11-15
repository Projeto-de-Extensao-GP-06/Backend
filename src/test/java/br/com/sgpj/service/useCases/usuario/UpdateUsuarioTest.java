package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.response.Usuario.UsuarioListarResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.CargoPermissao;
import br.com.sgpj.entity.CargoPermissaoId;
import br.com.sgpj.entity.Permissao;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.exception.UltimoAdministradorException;
import br.com.sgpj.exception.UsuarioAutoExclusaoException;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateUsuario - Regras de negócio de atualização de usuário")
class UpdateUsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    @InjectMocks
    private UpdateUsuario updateUsuario;

    @Test
    @DisplayName("Não deve permitir que o usuário altere o próprio cargo")
    void naoDevePermitirAlterarProprioCargo() {
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Usuario logado = new Usuario(10L, "Admin", "admin@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@sgpj.com");
        when(usuarioRepository.findByEmail("admin@sgpj.com")).thenReturn(logado);

        UsuarioAtualizacaoRequest req = new UsuarioAtualizacaoRequest("Admin", "admin@sgpj.com", 3L, true);

        assertThatThrownBy(() -> updateUsuario.executar(req, 10L))
                .isInstanceOf(UsuarioAutoExclusaoException.class)
                .hasMessageContaining("próprio cargo");
    }

    @Test
    @DisplayName("Não deve permitir que o usuário desative a si mesmo")
    void naoDevePermitirAutoDesativacao() {
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Usuario logado = new Usuario(10L, "Admin", "admin@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@sgpj.com");
        when(usuarioRepository.findByEmail("admin@sgpj.com")).thenReturn(logado);

        UsuarioAtualizacaoRequest req = new UsuarioAtualizacaoRequest("Admin", "admin@sgpj.com", 1L, false);

        assertThatThrownBy(() -> updateUsuario.executar(req, 10L))
                .isInstanceOf(UsuarioAutoExclusaoException.class)
                .hasMessageContaining("desativar sua própria conta");
    }

    @Test
    @DisplayName("Não deve permitir que o usuário altere os próprios dados pessoais pela rota administrativa")
    void naoDevePermitirAlterarPropriosDadosPessoaisPelaRotaDeUsuarios() {
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Usuario logado = new Usuario(10L, "Admin", "admin@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@sgpj.com");
        when(usuarioRepository.findByEmail("admin@sgpj.com")).thenReturn(logado);

        UsuarioAtualizacaoRequest req = new UsuarioAtualizacaoRequest("Novo Nome", "admin@sgpj.com", 1L, true);

        assertThatThrownBy(() -> updateUsuario.executar(req, 10L))
                .isInstanceOf(UsuarioAutoExclusaoException.class)
                .hasMessageContaining("Meu Perfil");
    }

    @Test
    @DisplayName("Não deve permitir rebaixar ou desativar o único administrador ativo do sistema")
    void naoDevePermitirRebaixarUnicoAdministradorAtivo() {
        Cargo cargoEditor = new Cargo(3L, "Gestor", null, LocalDateTime.now());
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Permissao todasPerms = new Permissao(18L, "TODAS AS PERMISSÕES");
        cargoAdmin.setPermissoes(List.of(new CargoPermissao(new CargoPermissaoId(1L, 18L), cargoAdmin, todasPerms, null, LocalDateTime.now())));

        Usuario logado = new Usuario(20L, "Gestor", "gestor@sgpj.com", "hash", cargoEditor, LocalDateTime.now(), true);
        Usuario unicoAdmin = new Usuario(10L, "Unico Admin", "admin@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("gestor@sgpj.com");
        when(usuarioRepository.findByEmail("gestor@sgpj.com")).thenReturn(logado);
        when(usuarioRepository.existsById(10L)).thenReturn(true);
        when(usuarioRepository.getReferenceById(10L)).thenReturn(unicoAdmin);
        when(cargoRepository.findById(3L)).thenReturn(Optional.of(cargoEditor));
        when(usuarioRepository.findAll()).thenReturn(List.of(logado, unicoAdmin));

        UsuarioAtualizacaoRequest reqRebaixar = new UsuarioAtualizacaoRequest("Unico Admin", "admin@sgpj.com", 3L, true);

        assertThatThrownBy(() -> updateUsuario.executar(reqRebaixar, 10L))
                .isInstanceOf(UltimoAdministradorException.class)
                .hasMessageContaining("único administrador ativo");
    }

    @Test
    @DisplayName("Deve permitir atualizar outro usuário quando houver outro administrador ativo")
    void deveAtualizarOutroUsuarioComSucesso() {
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Cargo cargoAdvogado = new Cargo(3L, "Advogado", null, LocalDateTime.now());

        Usuario adminLogado = new Usuario(10L, "Admin 1", "admin1@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);
        Usuario alvo = new Usuario(20L, "Advogado", "adv@sgpj.com", "hash", cargoAdvogado, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin1@sgpj.com");
        when(usuarioRepository.findByEmail("admin1@sgpj.com")).thenReturn(adminLogado);
        when(usuarioRepository.existsById(20L)).thenReturn(true);
        when(usuarioRepository.getReferenceById(20L)).thenReturn(alvo);
        when(cargoRepository.findById(3L)).thenReturn(Optional.of(cargoAdvogado));

        UsuarioAtualizacaoRequest req = new UsuarioAtualizacaoRequest("Advogado Editado", "adv@sgpj.com", 3L, true);

        UsuarioListarResponse resp = updateUsuario.executar(req, 20L);

        assertThat(resp.nome()).isEqualTo("Advogado Editado");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve permitir atualizar e-mail para um já utilizado por outro usuário")
    void naoDevePermitirEmailDuplicado() {
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Usuario adminLogado = new Usuario(10L, "Admin 1", "admin1@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);
        Usuario alvo = new Usuario(20L, "Advogado", "adv@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin1@sgpj.com");
        when(usuarioRepository.findByEmail("admin1@sgpj.com")).thenReturn(adminLogado);
        when(usuarioRepository.existsById(20L)).thenReturn(true);
        when(usuarioRepository.getReferenceById(20L)).thenReturn(alvo);
        when(usuarioRepository.existsByEmail("existente@sgpj.com")).thenReturn(true);

        UsuarioAtualizacaoRequest req = new UsuarioAtualizacaoRequest("Advogado", "existente@sgpj.com", 1L, true);

        assertThatThrownBy(() -> updateUsuario.executar(req, 20L))
                .isInstanceOf(EmailDuplicadoException.class);
        verifyNoMoreInteractions(cargoRepository);
    }
}
