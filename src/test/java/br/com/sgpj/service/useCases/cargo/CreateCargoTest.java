package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CreateCargoTest {

    @Mock
    private CargoRepository cargoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PermissaoRepository permissaoRepository;

    @Mock
    private CargoPermissaoRepository cargoPermissaoRepository;

    @InjectMocks
    private CreateCargo createCargo;

    @Test
    void naoCriaCargoComNomeReservadoMesmoComVariacoesDeCaixaEEspacos() {
        assertThatThrownBy(() -> createCargo.criarCargo(new CriarCargoRequest(" admin "), "user@test.com"))
                .isInstanceOf(CargoJaExisteException.class)
                .hasMessageContaining("reservado");
        assertThatThrownBy(() -> createCargo.criarCargo(new CriarCargoRequest("SEM CARGO"), "user@test.com"))
                .isInstanceOf(CargoJaExisteException.class)
                .hasMessageContaining("reservado");

        verifyNoInteractions(cargoRepository, usuarioRepository, permissaoRepository, cargoPermissaoRepository);
    }
}
