package br.com.sgpj.controller;

import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.service.CargoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CargoController - Testes Unitários")
class CargoControllerTest {

    @Mock
    private CargoService cargoService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private CargoResponse cargoResponse;

    @Mock
    private CriarCargoRequest criarCargoRequest;

    @Mock
    private AtribuirPermissoesRequest atribuirPermissoesRequest;

    @InjectMocks
    private CargoController cargoController;

    private static final String EMAIL_USUARIO = "usuario@teste.com";
    private static final Long CARGO_ID = 1L;

    private void configurarSecurityContext() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(EMAIL_USUARIO);
    }

    // =========================================================================
    // listarCargosPaginados
    // =========================================================================

    @Nested
    @DisplayName("GET /cargos - listarCargosPaginados")
    class ListarCargosPaginados {

        @Test
        @DisplayName("Deve retornar página de cargos com status 200 quando escopo é válido")
        void deveRetornarPaginaDeCargoQuandoEscopoValido() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CargoResponse> paginaEsperada = new PageImpl<>(List.of(cargoResponse));

            when(cargoService.listarPaginado(pageable)).thenReturn(paginaEsperada);

            ResponseEntity<Page<CargoResponse>> resposta = cargoController.listarCargosPaginados(pageable);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().getContent()).hasSize(1);

            verify(cargoService).listarPaginado(pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia com status 200 quando não há cargos cadastrados")
        void deveRetornarPaginaVaziaQuandoNaoHaCargos() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<CargoResponse> paginaVazia = new PageImpl<>(List.of());

            when(cargoService.listarPaginado(pageable)).thenReturn(paginaVazia);

            ResponseEntity<Page<CargoResponse>> resposta = cargoController.listarCargosPaginados(pageable);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody().getContent()).isEmpty();
        }
    }

    // =========================================================================
    // criar
    // =========================================================================

    @Nested
    @DisplayName("POST /cargos - criar")
    class Criar {

        @Test
        @DisplayName("Deve criar cargo e retornar status 201 quando escopo é válido")
        void deveCriarCargoComStatus201QuandoEscopoValido() {
            configurarSecurityContext();
            when(cargoService.criar(criarCargoRequest, EMAIL_USUARIO)).thenReturn(cargoResponse);

            ResponseEntity<CargoResponse> resposta = cargoController.criar(criarCargoRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody()).isEqualTo(cargoResponse);

            verify(cargoService).criar(criarCargoRequest, EMAIL_USUARIO);
        }

        @Test
        @DisplayName("Deve extrair o email do SecurityContext ao criar cargo")
        void deveExtrairEmailDoSecurityContextAoCriarCargo() {
            configurarSecurityContext();
            when(cargoService.criar(any(), eq(EMAIL_USUARIO))).thenReturn(cargoResponse);

            cargoController.criar(criarCargoRequest);

            verify(authentication).getName();
            verify(cargoService).criar(criarCargoRequest, EMAIL_USUARIO);
        }
    }

    // =========================================================================
    // atualizarPermissoes
    // =========================================================================

    @Nested
    @DisplayName("PUT /cargos/{cargoId}/permissoes - atualizarPermissoes")
    class AtualizarPermissoes {

        @Test
        @DisplayName("Deve atualizar permissões e retornar status 200 quando escopo é válido")
        void deveAtualizarPermissoesComStatus200QuandoEscopoValido() {
            configurarSecurityContext();
            when(cargoService.atualizarPermissoes(CARGO_ID, atribuirPermissoesRequest, EMAIL_USUARIO))
                    .thenReturn(cargoResponse);

            ResponseEntity<CargoResponse> resposta = cargoController.atualizarPermissoes(
                    CARGO_ID, atribuirPermissoesRequest);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isNotNull();
            assertThat(resposta.getBody()).isEqualTo(cargoResponse);

            verify(cargoService).atualizarPermissoes(CARGO_ID, atribuirPermissoesRequest, EMAIL_USUARIO);
        }

        @Test
        @DisplayName("Deve extrair o email do SecurityContext ao atualizar permissões")
        void deveExtrairEmailDoSecurityContextAoAtualizarPermissoes() {
            configurarSecurityContext();
            when(cargoService.atualizarPermissoes(any(), any(), eq(EMAIL_USUARIO))).thenReturn(cargoResponse);

            cargoController.atualizarPermissoes(CARGO_ID, atribuirPermissoesRequest);

            verify(authentication).getName();
            verify(cargoService).atualizarPermissoes(CARGO_ID, atribuirPermissoesRequest, EMAIL_USUARIO);
        }
    }

    // =========================================================================
    // deletar
    // =========================================================================

    @Nested
    @DisplayName("DELETE /cargos/{cargoId} - deletar")
    class Deletar {

        @Test
        @DisplayName("Deve deletar cargo e retornar status 204 quando escopo é válido")
        void deveDeletarCargoComStatus204QuandoEscopoValido() {
            doNothing().when(cargoService).deletar(CARGO_ID);

            ResponseEntity<Void> resposta = cargoController.deletar(CARGO_ID);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();

            verify(cargoService).deletar(CARGO_ID);
        }

        @Test
        @DisplayName("Deve propagar exceção lançada pelo service ao deletar cargo")
        void devePropagaExcecaoDoServiceAoDeletarCargo() {
            doThrow(new RuntimeException("Cargo não encontrado")).when(cargoService).deletar(CARGO_ID);

            assertThatThrownBy(() -> cargoController.deletar(CARGO_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Cargo não encontrado");

            verify(cargoService).deletar(CARGO_ID);
        }
    }
}