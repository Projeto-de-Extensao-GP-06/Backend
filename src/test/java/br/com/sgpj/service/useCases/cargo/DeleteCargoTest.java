package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.exception.UltimoAdministradorException;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCargoTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoPermissaoRepository cargoPermissaoRepository;

    @InjectMocks
    private DeleteCargo deleteCargo;

    @Test
    void naoExcluiCargosDoSistema() {
        assertThatThrownBy(() -> deleteCargo.deletarCargo(1L))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("ADMIN");
        assertThatThrownBy(() -> deleteCargo.deletarCargo(2L))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("Sem Cargo");

        verifyNoInteractions(cargoRepository, usuarioRepository, cargoPermissaoRepository);
    }

    @Test
    void naoExcluiCargoComUnicoAdministradorAtivoDoSistema() {
        Cargo cargoCustomAdmin = new Cargo(3L, "Diretor", null, LocalDateTime.now());
        Usuario unicoAdmin = new Usuario(10L, "Diretor", "diretor@sgpj.com", "hash", cargoCustomAdmin, LocalDateTime.now(), true);

        when(cargoRepository.existsById(3L)).thenReturn(true);
        when(usuarioRepository.existsByCargo_Id(3L)).thenReturn(true);
        when(usuarioRepository.findAllByCargo_Id(3L)).thenReturn(List.of(unicoAdmin));
        when(cargoPermissaoRepository.existsById_CargoIdAndId_PermissaoId(3L, 18L)).thenReturn(true);
        when(usuarioRepository.findAll()).thenReturn(List.of(unicoAdmin));

        assertThatThrownBy(() -> deleteCargo.deletarCargo(3L))
                .isInstanceOf(UltimoAdministradorException.class)
                .hasMessageContaining("único administrador ativo");
    }
}
