package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UltimoAdministradorException;
import br.com.sgpj.exception.UsuarioAutoExclusaoException;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteUsuario - Regras de negócio de desativação/exclusão de usuário")
class DeleteUsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    @InjectMocks
    private DeleteUsuario deleteUsuario;

    @Test
    @DisplayName("Não deve permitir que o usuário desative/exclua a si mesmo")
    void naoDevePermitirAutoExclusao() {
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Usuario logado = new Usuario(10L, "Admin", "admin@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@sgpj.com");
        when(usuarioRepository.findByEmail("admin@sgpj.com")).thenReturn(logado);

        assertThatThrownBy(() -> deleteUsuario.executar(10L))
                .isInstanceOf(UsuarioAutoExclusaoException.class)
                .hasMessageContaining("própria conta");
    }

    @Test
    @DisplayName("Não deve permitir desativar o único administrador ativo do sistema")
    void naoDevePermitirDesativarUnicoAdministrador() {
        Cargo cargoGestor = new Cargo(3L, "Gestor", null, LocalDateTime.now());
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());

        Usuario gestorLogado = new Usuario(20L, "Gestor", "gestor@sgpj.com", "hash", cargoGestor, LocalDateTime.now(), true);
        Usuario unicoAdmin = new Usuario(10L, "Admin", "admin@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("gestor@sgpj.com");
        when(usuarioRepository.findByEmail("gestor@sgpj.com")).thenReturn(gestorLogado);
        when(usuarioRepository.existsById(10L)).thenReturn(true);
        when(usuarioRepository.getReferenceById(10L)).thenReturn(unicoAdmin);
        when(usuarioRepository.findAll()).thenReturn(List.of(gestorLogado, unicoAdmin));

        assertThatThrownBy(() -> deleteUsuario.executar(10L))
                .isInstanceOf(UltimoAdministradorException.class)
                .hasMessageContaining("único administrador ativo");
    }

    @Test
    @DisplayName("Deve desativar usuário mantendo seu cargo atual quando válido")
    void deveDesativarMantendoCargo() {
        Cargo cargoAdmin = new Cargo(1L, "ADMIN", null, LocalDateTime.now());
        Cargo cargoAdvogado = new Cargo(3L, "Advogado", null, LocalDateTime.now());

        Usuario adminLogado = new Usuario(10L, "Admin", "admin@sgpj.com", "hash", cargoAdmin, LocalDateTime.now(), true);
        Usuario alvo = new Usuario(20L, "Advogado", "adv@sgpj.com", "hash", cargoAdvogado, LocalDateTime.now(), true);

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("admin@sgpj.com");
        when(usuarioRepository.findByEmail("admin@sgpj.com")).thenReturn(adminLogado);
        when(usuarioRepository.existsById(20L)).thenReturn(true);
        when(usuarioRepository.getReferenceById(20L)).thenReturn(alvo);

        deleteUsuario.executar(20L);

        assertThat(alvo.isAtivo()).isFalse();
        assertThat(alvo.getCargo()).isEqualTo(cargoAdvogado);
        verify(usuarioRepository).save(alvo);
    }
}
