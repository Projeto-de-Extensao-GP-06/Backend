package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.dto.request.RenomearCargoRequest;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateCargoTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoPermissaoRepository cargoPermissaoRepository;

    @Mock
    private PermissaoRepository permissaoRepository;

    @Mock
    private AtribuirPermissaoEspecial atribuirPermissaoEspecial;

    @InjectMocks
    private UpdateCargo updateCargo;

    @Test
    void naoRenomeiaCargosDoSistema() {
        assertThatThrownBy(() -> updateCargo.renomearCargo(1L, new RenomearCargoRequest("Novo nome")))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("ADMIN");
        assertThatThrownBy(() -> updateCargo.renomearCargo(2L, new RenomearCargoRequest("Novo nome")))
                .isInstanceOf(OperacaoInvalidaException.class)
                .hasMessageContaining("Sem Cargo");

        verifyNoInteractions(cargoRepository);
    }

    @Test
    void renomeiaCargoExistenteComNomeAparado() {
        Cargo cargo = cargo(3L, "Nome antigo");
        when(cargoRepository.findById(3L)).thenReturn(Optional.of(cargo));
        when(cargoRepository.existsByNomeIgnoreCaseAndIdNot("Novo nome", 3L)).thenReturn(false);

        var response = updateCargo.renomearCargo(3L, new RenomearCargoRequest("  Novo nome  "));

        assertThat(response.nome()).isEqualTo("Novo nome");
        assertThat(cargo.getNome()).isEqualTo("Novo nome");
    }

    @Test
    void naoRenomeiaCargoParaNomeReservadoOuDuplicado() {
        Cargo cargo = cargo(3L, "Nome antigo");
        when(cargoRepository.findById(3L)).thenReturn(Optional.of(cargo));
        when(cargoRepository.existsByNomeIgnoreCaseAndIdNot("Outro cargo", 3L)).thenReturn(true);

        assertThatThrownBy(() -> updateCargo.renomearCargo(3L, new RenomearCargoRequest(" admin ")))
                .isInstanceOf(CargoJaExisteException.class)
                .hasMessageContaining("reservado");
        assertThatThrownBy(() -> updateCargo.renomearCargo(3L, new RenomearCargoRequest("Outro cargo")))
                .isInstanceOf(CargoJaExisteException.class)
                .hasMessageContaining("já existe");
    }

    private Cargo cargo(Long id, String nome) {
        Cargo cargo = new Cargo(id, nome, null, LocalDateTime.now());
        cargo.setPermissoes(List.of());
        return cargo;
    }
}
