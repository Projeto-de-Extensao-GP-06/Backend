package br.com.sgpj.service;

import br.com.sgpj.adapter.database.CargoPermissaoAdapter;
import br.com.sgpj.adapter.database.CargoPersistenciaAdapter;
import br.com.sgpj.adapter.database.PermissaoPersistenciaAdapter;
import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.AtribuirPermissoesRequest;
import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CargoService")
class CargoServiceTest {

    @Mock
    private CargoPersistenciaAdapter cargoPersistenciaAdapter;

    @Mock
    private PermissaoPersistenciaAdapter permissaoPersistenciaAdapter;

    @Mock
    private UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;

    @Mock
    private CargoPermissaoAdapter cargoPermissaoAdapter;

    @InjectMocks
    private CargoService cargoService;

    private static final Long ID_TESTE = 3L;
    private static final Long CARGO_ADMIN_ID = 1L;
    private static final Long SEM_CARGO_ID = 2L;
    private static final String NOME_CARGO_TESTE = "Analista";
    private static final String EMAIL_USUARIO_TESTE = "usuario@teste.com";
    private static final Long TODAS_PERMISSOES_ID = 18L;
    private static final Long SEM_PERMISSOES_ID = 19L;

    // ========================
    // listarPaginado()
    // ========================

    @Test
    @DisplayName("Deve listar cargos paginados com sucesso")
    void deveListarCargosPaginadosComSucesso() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Long> idsPage = new PageImpl<>(List.of(ID_TESTE), pageable, 1);
        Cargo cargo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        cargo.setPermissoes(List.of());

        when(cargoPersistenciaAdapter.findAllIds(pageable)).thenReturn(idsPage);
        when(cargoPersistenciaAdapter.findAllWithPermissoesByIds(idsPage)).thenReturn(List.of(cargo));

        // When
        Page<CargoResponse> resultado = cargoService.listarPaginado(pageable);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.getTotalElements());
        verify(cargoPersistenciaAdapter, times(1)).findAllIds(pageable);
        verify(cargoPersistenciaAdapter, times(1)).findAllWithPermissoesByIds(idsPage);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não houver cargos")
    void deveRetornarPaginaVaziaQuandoNaoHouverCargos() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Long> idsPage = new PageImpl<>(List.of(), pageable, 0);

        when(cargoPersistenciaAdapter.findAllIds(pageable)).thenReturn(idsPage);
        when(cargoPersistenciaAdapter.findAllWithPermissoesByIds(idsPage)).thenReturn(List.of());

        // When
        Page<CargoResponse> resultado = cargoService.listarPaginado(pageable);

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve preservar metadados de paginação ao listar")
    void devePreservarMetadadosDePaginacaoAoListar() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Long> idsPage = new PageImpl<>(List.of(ID_TESTE), pageable, 20);
        Cargo cargo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        cargo.setPermissoes(List.of());

        when(cargoPersistenciaAdapter.findAllIds(pageable)).thenReturn(idsPage);
        when(cargoPersistenciaAdapter.findAllWithPermissoesByIds(idsPage)).thenReturn(List.of(cargo));

        // When
        Page<CargoResponse> resultado = cargoService.listarPaginado(pageable);

        // Then
        assertEquals(20, resultado.getTotalElements());
        assertEquals(2, resultado.getTotalPages());
    }

    // ========================
    // criar()
    // ========================

    @Test
    @DisplayName("Deve criar cargo com sucesso")
    void deveCriarCargoComSucesso() {
        // Given
        CriarCargoRequest request = mock(CriarCargoRequest.class);
        Usuario usuario = criarUsuario();
        Cargo cargoSalvo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        Permissao semPermissoes = criarPermissao(SEM_PERMISSOES_ID, "Sem permissões");

        when(request.nome()).thenReturn(NOME_CARGO_TESTE);
        when(cargoPersistenciaAdapter.existsByNome(NOME_CARGO_TESTE)).thenReturn(false);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_USUARIO_TESTE)).thenReturn(usuario);
        when(cargoPersistenciaAdapter.salvar(any(Cargo.class))).thenReturn(cargoSalvo);
        when(permissaoPersistenciaAdapter.buscarPermissaoPorId(SEM_PERMISSOES_ID)).thenReturn(semPermissoes);

        // When
        CargoResponse resultado = cargoService.criar(request, EMAIL_USUARIO_TESTE);

        // Then
        assertNotNull(resultado);
        assertEquals(ID_TESTE, resultado.cargoId());
        assertEquals(NOME_CARGO_TESTE, resultado.nome());
        verify(cargoPersistenciaAdapter, times(1)).salvar(any(Cargo.class));
    }

    @Test
    @DisplayName("Deve atribuir permissão 'Sem permissões' ao criar cargo")
    void deveAtribuirPermissaoSemPermissoesAoCriarCargo() {
        // Given
        CriarCargoRequest request = mock(CriarCargoRequest.class);
        Usuario usuario = criarUsuario();
        Cargo cargoSalvo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        Permissao semPermissoes = criarPermissao(SEM_PERMISSOES_ID, "Sem permissões");

        when(request.nome()).thenReturn(NOME_CARGO_TESTE);
        when(cargoPersistenciaAdapter.existsByNome(NOME_CARGO_TESTE)).thenReturn(false);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_USUARIO_TESTE)).thenReturn(usuario);
        when(cargoPersistenciaAdapter.salvar(any(Cargo.class))).thenReturn(cargoSalvo);
        when(permissaoPersistenciaAdapter.buscarPermissaoPorId(SEM_PERMISSOES_ID)).thenReturn(semPermissoes);

        // When
        CargoResponse resultado = cargoService.criar(request, EMAIL_USUARIO_TESTE);

        // Then
        assertNotNull(resultado.permissoes());
        assertEquals(1, resultado.permissoes().size());
        assertEquals("Sem permissões", resultado.permissoes().getFirst().nome());
        verify(cargoPermissaoAdapter, times(1)).salvar(any(CargoPermissao.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar cargo com nome duplicado")
    void deveLancarExcecaoAoCriarCargoComNomeDuplicado() {
        // Given
        CriarCargoRequest request = mock(CriarCargoRequest.class);
        when(request.nome()).thenReturn(NOME_CARGO_TESTE);
        when(cargoPersistenciaAdapter.existsByNome(NOME_CARGO_TESTE)).thenReturn(true);

        // When/Then
        assertThrows(
                CargoJaExisteException.class,
                () -> cargoService.criar(request, EMAIL_USUARIO_TESTE)
        );

        verify(cargoPersistenciaAdapter, never()).salvar(any(Cargo.class));
    }

    @Test
    @DisplayName("Não deve salvar cargo quando nome já existe")
    void naoDeveSalvarCargoQuandoNomeJaExiste() {
        // Given
        CriarCargoRequest request = mock(CriarCargoRequest.class);
        when(request.nome()).thenReturn(NOME_CARGO_TESTE);
        when(cargoPersistenciaAdapter.existsByNome(NOME_CARGO_TESTE)).thenReturn(true);

        // When/Then
        assertThrows(CargoJaExisteException.class, () -> cargoService.criar(request, EMAIL_USUARIO_TESTE));

        verify(usuarioPersistenciaAdapter, never()).buscarUsuarioPorEmail(anyString());
        verify(cargoPersistenciaAdapter, never()).salvar(any(Cargo.class));
    }

    // ========================
    // atualizarPermissoes()
    // ========================

    @Test
    @DisplayName("Deve atualizar permissões do cargo com sucesso")
    void deveAtualizarPermissoesDoCargoComSucesso() {
        // Given
        Long permissaoId = 10L;
        AtribuirPermissoesRequest request = mock(AtribuirPermissoesRequest.class);
        Cargo cargo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        Usuario usuario = criarUsuario();
        Permissao permissao = criarPermissao(permissaoId, "VISUALIZAR_RELATORIO");
        CargoPermissao cargoPermissao = criarCargoPermissao(cargo, permissao, usuario);

        when(request.permissaoIds()).thenReturn(List.of(permissaoId));
        when(cargoPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_USUARIO_TESTE)).thenReturn(usuario);
        when(permissaoPersistenciaAdapter.buscarPermissaoPorId(permissaoId)).thenReturn(permissao);
        when(cargoPermissaoAdapter.findAllById_CargoId(ID_TESTE)).thenReturn(List.of(cargoPermissao));

        // When
        CargoResponse resultado = cargoService.atualizarPermissoes(ID_TESTE, request, EMAIL_USUARIO_TESTE);

        // Then
        assertNotNull(resultado);
        assertFalse(resultado.permissoes().isEmpty());
        assertEquals(1, resultado.permissoes().size());
        assertEquals(permissaoId, resultado.permissoes().getFirst().id());
    }

    @Test
    @DisplayName("Deve remover permissões anteriores ao atualizar")
    void deveRemoverPermissoesAnterioresAoAtualizar() {
        // Given
        AtribuirPermissoesRequest request = mock(AtribuirPermissoesRequest.class);
        Cargo cargo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        Usuario usuario = criarUsuario();

        when(request.permissaoIds()).thenReturn(List.of());
        when(cargoPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_USUARIO_TESTE)).thenReturn(usuario);
        when(cargoPermissaoAdapter.findAllById_CargoId(ID_TESTE)).thenReturn(List.of());

        // When
        cargoService.atualizarPermissoes(ID_TESTE, request, EMAIL_USUARIO_TESTE);

        // Then
        verify(cargoPermissaoAdapter, times(1)).deleteAllById_CargoId(ID_TESTE);
    }

    @Test
    @DisplayName("Deve salvar cada permissão informada ao atualizar")
    void deveSalvarCadaPermissaoInformadaAoAtualizar() {
        // Given
        List<Long> permissaoIds = List.of(10L, 11L);
        AtribuirPermissoesRequest request = mock(AtribuirPermissoesRequest.class);
        Cargo cargo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        Usuario usuario = criarUsuario();

        when(request.permissaoIds()).thenReturn(permissaoIds);
        when(cargoPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_USUARIO_TESTE)).thenReturn(usuario);
        when(permissaoPersistenciaAdapter.buscarPermissaoPorId(anyLong()))
                .thenAnswer(inv -> criarPermissao(inv.getArgument(0), "PERMISSAO_" + inv.getArgument(0)));
        when(cargoPermissaoAdapter.findAllById_CargoId(ID_TESTE)).thenReturn(List.of());

        // When
        cargoService.atualizarPermissoes(ID_TESTE, request, EMAIL_USUARIO_TESTE);

        // Then
        verify(cargoPermissaoAdapter, times(2)).salvar(any(CargoPermissao.class));
    }

    @Test
    @DisplayName("Deve atribuir 'Sem permissões' ao atualizar com lista vazia")
    void deveAtribuirSemPermissoesAoAtualizarComListaVazia() {
        // Given
        AtribuirPermissoesRequest request = mock(AtribuirPermissoesRequest.class);
        Cargo cargo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        Usuario usuario = criarUsuario();
        Permissao semPermissoes = criarPermissao(SEM_PERMISSOES_ID, "Sem permissões");
        CargoPermissao cargoPermissao = criarCargoPermissao(cargo, semPermissoes, usuario);

        when(request.permissaoIds()).thenReturn(List.of());
        when(cargoPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_USUARIO_TESTE)).thenReturn(usuario);
        when(permissaoPersistenciaAdapter.buscarPermissaoPorId(SEM_PERMISSOES_ID)).thenReturn(semPermissoes);
        when(cargoPermissaoAdapter.findAllById_CargoId(ID_TESTE)).thenReturn(List.of(cargoPermissao));

        // When
        CargoResponse resultado = cargoService.atualizarPermissoes(ID_TESTE, request, EMAIL_USUARIO_TESTE);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.permissoes().size());
        assertEquals("Sem permissões", resultado.permissoes().getFirst().nome());
        verify(cargoPermissaoAdapter, times(1)).salvar(any(CargoPermissao.class));
        verify(permissaoPersistenciaAdapter, times(1)).buscarPermissaoPorId(SEM_PERMISSOES_ID);
    }

    @Test
    @DisplayName("Deve atribuir 'Todas as permissões' ao atualizar com as 20 permissões normais")
    void deveAtribuirTodasAsPermissoesAoAtualizarComAs20PermissoesNormais() {
        // Given
        List<Long> ids17Permissoes = LongStream.rangeClosed(1, 17)
                .boxed()
                .toList();

        AtribuirPermissoesRequest request = mock(AtribuirPermissoesRequest.class);
        Cargo cargo = criarCargo(ID_TESTE, NOME_CARGO_TESTE);
        Usuario usuario = criarUsuario();
        Permissao todasPermissoes = criarPermissao(TODAS_PERMISSOES_ID, "Todas as permissões");
        CargoPermissao cargoPermissao = criarCargoPermissao(cargo, todasPermissoes, usuario);

        when(request.permissaoIds()).thenReturn(ids17Permissoes);
        when(cargoPersistenciaAdapter.buscarCargoPorId(ID_TESTE)).thenReturn(cargo);
        when(usuarioPersistenciaAdapter.buscarUsuarioPorEmail(EMAIL_USUARIO_TESTE)).thenReturn(usuario);
        when(permissaoPersistenciaAdapter.buscarPermissaoPorId(TODAS_PERMISSOES_ID)).thenReturn(todasPermissoes);
        when(cargoPermissaoAdapter.findAllById_CargoId(ID_TESTE)).thenReturn(List.of(cargoPermissao));

        // When
        CargoResponse resultado = cargoService.atualizarPermissoes(ID_TESTE, request, EMAIL_USUARIO_TESTE);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.permissoes().size());
        assertEquals("Todas as permissões", resultado.permissoes().getFirst().nome());
        verify(cargoPermissaoAdapter, times(1)).salvar(any(CargoPermissao.class));
        verify(permissaoPersistenciaAdapter, times(1)).buscarPermissaoPorId(TODAS_PERMISSOES_ID);
        verify(permissaoPersistenciaAdapter, never()).buscarPermissaoPorId(SEM_PERMISSOES_ID);
    }

    // ========================
    // deletar()
    // ========================

    @Test
    @DisplayName("Deve deletar cargo sem usuários associados com sucesso")
    void deveDeletarCargoSemUsuariosAssociadosComSucesso() {
        // Given
        when(cargoPersistenciaAdapter.existsById(ID_TESTE)).thenReturn(true);
        when(usuarioPersistenciaAdapter.existsByCargo(ID_TESTE)).thenReturn(false);

        // When
        cargoService.deletar(ID_TESTE);

        // Then
        verify(cargoPermissaoAdapter, times(1)).deleteAllById_CargoId(ID_TESTE);
        verify(cargoPersistenciaAdapter, times(1)).deleteById(ID_TESTE);
    }

    @Test
    @DisplayName("Deve migrar usuários para cargo padrão ao deletar cargo com usuários associados")
    void deveMigrarUsuariosParaCargoPadraoAoDeletarCargoComUsuariosAssociados() {
        // Given
        Usuario usuario = criarUsuario();
        Cargo cargoPadrao = criarCargo(SEM_CARGO_ID, "Sem Cargo");

        when(cargoPersistenciaAdapter.existsById(ID_TESTE)).thenReturn(true);
        when(usuarioPersistenciaAdapter.existsByCargo(ID_TESTE)).thenReturn(true);
        when(cargoPersistenciaAdapter.buscarCargoPorId(SEM_CARGO_ID)).thenReturn(cargoPadrao);
        when(usuarioPersistenciaAdapter.findAllByCargo_Id(ID_TESTE)).thenReturn(List.of(usuario));

        // When
        cargoService.deletar(ID_TESTE);

        // Then
        assertEquals(cargoPadrao, usuario.getCargo());
        verify(cargoPermissaoAdapter, times(1)).deleteAllById_CargoId(ID_TESTE);
        verify(cargoPersistenciaAdapter, times(1)).deleteById(ID_TESTE);
    }

    @Test
    @DisplayName("Deve remover permissões do cargo antes de deletar")
    void deveRemoverPermissoesDoCargoAntesDeDelear() {
        // Given
        when(cargoPersistenciaAdapter.existsById(ID_TESTE)).thenReturn(true);
        when(usuarioPersistenciaAdapter.existsByCargo(ID_TESTE)).thenReturn(false);

        // When
        cargoService.deletar(ID_TESTE);

        // Then
        verify(cargoPermissaoAdapter, times(1)).deleteAllById_CargoId(ID_TESTE);
        verify(cargoPersistenciaAdapter, times(1)).deleteById(ID_TESTE);
    }

    @Test
    @DisplayName("Deve lançar exceção ao deletar cargo não encontrado")
    void deveLancarExcecaoAoDeletarCargoNaoEncontrado() {
        // Given
        when(cargoPersistenciaAdapter.existsById(ID_TESTE)).thenReturn(false);

        // When/Then
        assertThrows(
                CargoNaoEncontradoException.class,
                () -> cargoService.deletar(ID_TESTE)
        );

        verify(cargoPersistenciaAdapter, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar o cargo ADMIN")
    void deveLancarExcecaoAoTentarDeletarCargoAdmin() {
        // When/Then
        assertThrows(
                OperacaoInvalidaException.class,
                () -> cargoService.deletar(CARGO_ADMIN_ID)
        );

        verify(cargoPersistenciaAdapter, never()).existsById(anyLong());
        verify(cargoPersistenciaAdapter, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar o cargo Sem Cargo")
    void deveLancarExcecaoAoTentarDeletarCargoSemCargo() {
        // When/Then
        assertThrows(
                OperacaoInvalidaException.class,
                () -> cargoService.deletar(SEM_CARGO_ID)
        );

        verify(cargoPersistenciaAdapter, never()).existsById(anyLong());
        verify(cargoPersistenciaAdapter, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Não deve verificar existência de cargo ao tentar deletar cargos protegidos")
    void naoDeveVerificarExistenciaDeCargoAoTentarDeletarCargosProtegidos() {
        // When/Then
        assertThrows(OperacaoInvalidaException.class, () -> cargoService.deletar(CARGO_ADMIN_ID));
        assertThrows(OperacaoInvalidaException.class, () -> cargoService.deletar(SEM_CARGO_ID));

        verify(cargoPersistenciaAdapter, never()).existsById(anyLong());
    }

    // ========================
    // Métodos auxiliares
    // ========================

    private Cargo criarCargo(Long id, String nome) {
        Cargo cargo = new Cargo();
        cargo.setId(id);
        cargo.setNome(nome);
        cargo.setCriadoEm(LocalDateTime.now());
        return cargo;
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setNome("Usuário Teste");
        usuario.setEmail(EMAIL_USUARIO_TESTE);
        usuario.setAtivo(true);
        return usuario;
    }

    private Permissao criarPermissao(Long id, String nome) {
        Permissao permissao = new Permissao();
        permissao.setId(id);
        permissao.setNome(nome);
        return permissao;
    }

    private CargoPermissao criarCargoPermissao(Cargo cargo, Permissao permissao, Usuario atribuidoPor) {
        return new CargoPermissao(
                new CargoPermissaoId(cargo.getId(), permissao.getId()),
                cargo,
                permissao,
                atribuidoPor,
                LocalDateTime.now()
        );
    }
}