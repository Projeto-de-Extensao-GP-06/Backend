package br.com.sgpj.service;

import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.request.RenomearCargoRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CargoService")
class CargoServiceTest {

    @Mock
    private br.com.sgpj.service.useCases.cargo.CreateCargo createCargo;

    @Mock
    private br.com.sgpj.service.useCases.cargo.ReadCargo readCargo;

    @Mock
    private br.com.sgpj.service.useCases.cargo.UpdateCargo updateCargo;

    @Mock
    private br.com.sgpj.service.useCases.cargo.DeleteCargo deleteCargo;

    @InjectMocks
    private CargoService cargoService;

    @Test
    @DisplayName("listarPaginado delega para ReadCargo")
    void listarPaginado_delegaParaReadCargo() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CargoResponse> page = new PageImpl<>(List.of(mock(CargoResponse.class)), pageable, 1);

        when(readCargo.listarPaginado(pageable)).thenReturn(page);

        Page<CargoResponse> resultado = cargoService.listarPaginado(pageable);

        assertSame(page, resultado);
        verify(readCargo, times(1)).listarPaginado(pageable);
    }

    @Test
    @DisplayName("criar delega para CreateCargo")
    void criar_delegaParaCreateCargo() {
        CriarCargoRequest req = mock(CriarCargoRequest.class);
        String email = "user@test.com";
        CargoResponse resp = mock(CargoResponse.class);

        when(createCargo.criarCargo(req, email)).thenReturn(resp);

        CargoResponse resultado = cargoService.criar(req, email);

        assertSame(resp, resultado);
        verify(createCargo, times(1)).criarCargo(req, email);
    }

    @Test
    @DisplayName("atualizarPermissoes delega para UpdateCargo")
    void atualizarPermissoes_delegaParaUpdateCargo() {
        Long id = 1L;
        var req = mock(br.com.sgpj.dto.request.AtribuirPermissoesRequest.class);
        String email = "user@test.com";
        CargoResponse resp = mock(CargoResponse.class);

        when(updateCargo.atualizarPermissoesCargo(id, req, email)).thenReturn(resp);

        CargoResponse resultado = cargoService.atualizarPermissoes(id, req, email);

        assertSame(resp, resultado);
        verify(updateCargo, times(1)).atualizarPermissoesCargo(id, req, email);
    }

    @Test
    @DisplayName("renomear delega para UpdateCargo")
    void renomear_delegaParaUpdateCargo() {
        Long id = 3L;
        RenomearCargoRequest req = new RenomearCargoRequest("Novo nome");
        CargoResponse resp = mock(CargoResponse.class);

        when(updateCargo.renomearCargo(id, req)).thenReturn(resp);

        CargoResponse resultado = cargoService.renomear(id, req);

        assertSame(resp, resultado);
        verify(updateCargo).renomearCargo(id, req);
    }

    @Test
    @DisplayName("deletar delega para DeleteCargo")
    void deletar_delegaParaDeleteCargo() {
        Long id = 2L;

        cargoService.deletar(id);

        verify(deleteCargo, times(1)).deletarCargo(id);
    }
}
