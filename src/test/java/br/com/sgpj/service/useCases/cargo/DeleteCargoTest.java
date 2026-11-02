package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

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
}
