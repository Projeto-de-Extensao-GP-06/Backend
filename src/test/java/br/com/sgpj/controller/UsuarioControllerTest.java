package br.com.sgpj.controller;

import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.request.UsuarioCriacaoRequest;
import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.service.UsuarioService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioController - Testes Unitários")
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @Nested
    @DisplayName("criar")
    class CriarTest {

        @Test
        @DisplayName("Deve retornar 201 Created e delegar a criação ao service")
        void deveCriarUsuarioComSucesso() {
            UsuarioCriacaoRequest request = mock(UsuarioCriacaoRequest.class);

            ResponseEntity<Void> resposta = usuarioController.criar(request);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(usuarioService).criar(request);
        }
    }

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTest {

        @Test
        @DisplayName("Deve retornar 200 com a lista quando houver usuários")
        void deveRetornarUsuariosQuandoExistirem() {
            List<UsuarioListarResponse> usuarios = List.of(mock(UsuarioListarResponse.class));
            when(usuarioService.listarTodos()).thenReturn(usuarios);

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarTodos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(usuarios);
        }

        @Test
        @DisplayName("Deve retornar 204 No Content quando não houver usuários")
        void deveRetornarNoContentQuandoListaVazia() {
            when(usuarioService.listarTodos()).thenReturn(List.of());

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarTodos();

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            assertThat(resposta.getBody()).isNull();
        }
    }

    @Nested
    @DisplayName("listarPorCargo")
    class ListarPorCargoTest {

        @Test
        @DisplayName("Deve retornar 200 com os usuários do cargo informado")
        void deveRetornarUsuariosDoCargo() {
            List<UsuarioListarResponse> usuarios = List.of(mock(UsuarioListarResponse.class));
            when(usuarioService.listarPorCargo("Advogado")).thenReturn(usuarios);

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarPorCargo("Advogado");

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(usuarios);
        }

        @Test
        @DisplayName("Deve retornar 204 No Content quando não houver usuários no cargo")
        void deveRetornarNoContentQuandoCargoSemUsuarios() {
            when(usuarioService.listarPorCargo("Estagiário")).thenReturn(List.of());

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.listarPorCargo("Estagiário");

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Nested
    @DisplayName("pesquisar")
    class PesquisarTest {

        @Test
        @DisplayName("Deve retornar 200 com os usuários encontrados")
        void deveRetornarUsuariosEncontrados() {
            List<UsuarioListarResponse> usuarios = List.of(mock(UsuarioListarResponse.class));
            when(usuarioService.buscarPorNomeOuEmail("joao")).thenReturn(usuarios);

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.pesquisar("joao");

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resposta.getBody()).isEqualTo(usuarios);
        }

        @Test
        @DisplayName("Deve retornar 204 No Content quando a pesquisa não encontrar ninguém")
        void deveRetornarNoContentQuandoPesquisaSemResultado() {
            when(usuarioService.buscarPorNomeOuEmail("inexistente")).thenReturn(List.of());

            ResponseEntity<List<UsuarioListarResponse>> resposta = usuarioController.pesquisar("inexistente");

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class AtualizarTest {

        @Test
        @DisplayName("Deve retornar 200 com o usuário atualizado")
        void deveAtualizarUsuarioComSucesso() {
            UsuarioAtualizacaoRequest request = mock(UsuarioAtualizacaoRequest.class);
            UsuarioListarResponse resposta = mock(UsuarioListarResponse.class);
            when(usuarioService.atualizarUsuario(1L, request)).thenReturn(resposta);

            ResponseEntity<UsuarioListarResponse> resultado = usuarioController.atualizar(1L, request);

            assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resultado.getBody()).isEqualTo(resposta);
        }
    }

    @Nested
    @DisplayName("deletar")
    class DeletarTest {

        @Test
        @DisplayName("Deve retornar 204 No Content e delegar a exclusão ao service")
        void deveDeletarUsuarioComSucesso() {
            ResponseEntity<Void> resposta = usuarioController.deletar(1L);

            assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(usuarioService).deletar(1L);
        }
    }

    @Nested
    @DisplayName("reativar")
    class ReativarTest {

        @Test
        @DisplayName("Deve retornar 200 com o usuário reativado")
        void deveReativarUsuarioComSucesso() {
            UsuarioListarResponse resposta = mock(UsuarioListarResponse.class);
            when(usuarioService.reativarUsuario(1L)).thenReturn(resposta);

            ResponseEntity<UsuarioListarResponse> resultado = usuarioController.reativar(1L);

            assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(resultado.getBody()).isEqualTo(resposta);
        }
    }
}
