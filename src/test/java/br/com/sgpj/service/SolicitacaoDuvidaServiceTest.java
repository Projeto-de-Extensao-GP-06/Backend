package br.com.sgpj.service;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoCandidatoDuplicidadeResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.service.useCases.solicitacaoDuvida.AceitarSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.BuscarCandidatosDuplicidadeSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.CreateSolicitacaoDuvida;
import br.com.sgpj.service.useCases.solicitacaoDuvida.FiltrarSolicitacoes;
import br.com.sgpj.service.useCases.solicitacaoDuvida.ListarSolicitacoesPendentes;
import br.com.sgpj.service.useCases.solicitacaoDuvida.NegarSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.ReadSolicitacaoDuvida;
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
@DisplayName("Testes do SolicitacaoDuvidaService")
class SolicitacaoDuvidaServiceTest {

    @Mock
    private CreateSolicitacaoDuvida createSolicitacaoDuvida;

    @Mock
    private ListarSolicitacoesPendentes listarSolicitacoesPendentes;

    @Mock
    private AceitarSolicitacao aceitarSolicitacao;

    @Mock
    private NegarSolicitacao negarSolicitacao;

    @Mock
    private ReadSolicitacaoDuvida readSolicitacaoDuvida;

    @Mock
    private FiltrarSolicitacoes filtrarSolicitacoes;

    @Mock
    private BuscarCandidatosDuplicidadeSolicitacao buscarCandidatosDuplicidadeSolicitacao;

    @InjectMocks
    private SolicitacaoDuvidaService solicitacaoDuvidaService;

    @Test
    @DisplayName("criar deve delegar ao use case CreateSolicitacaoDuvida")
    void deveDelegarCriacaoAoUseCase() {
        SolicitacaoDuvidaCriacaoRequest request = mock(SolicitacaoDuvidaCriacaoRequest.class);
        SolicitacaoDuvidaDetalhesResponse resposta = mock(SolicitacaoDuvidaDetalhesResponse.class);
        when(createSolicitacaoDuvida.criarSolicitacao(request)).thenReturn(resposta);

        SolicitacaoDuvidaDetalhesResponse resultado = solicitacaoDuvidaService.criar(request);

        assertEquals(resposta, resultado);
        verify(createSolicitacaoDuvida).criarSolicitacao(request);
    }

    @Test
    @DisplayName("listarPendentes deve delegar ao use case ListarSolicitacoesPendentes")
    void deveDelegarListagemDePendentesAoUseCase() {
        Pageable pageable = PageRequest.of(1, 2);
        Page<SolicitacaoDuvidaListarResponse> pagina = new PageImpl<>(
                List.of(mock(SolicitacaoDuvidaListarResponse.class)), pageable, 3);
        when(listarSolicitacoesPendentes.listarPendentes(pageable)).thenReturn(pagina);

        Page<SolicitacaoDuvidaListarResponse> resultado = solicitacaoDuvidaService.listarPendentes(pageable);

        assertEquals(pagina, resultado);
        assertEquals(1, resultado.getNumber());
        assertEquals(2, resultado.getSize());
        assertEquals(3, resultado.getTotalElements());
        assertEquals(2, resultado.getTotalPages());
        verify(listarSolicitacoesPendentes).listarPendentes(pageable);
    }

    @Test
    @DisplayName("buscarPorId deve delegar ao use case ReadSolicitacaoDuvida")
    void deveDelegarBuscaPorIdAoUseCase() {
        SolicitacaoDuvidaDetalhesResponse resposta = mock(SolicitacaoDuvidaDetalhesResponse.class);
        when(readSolicitacaoDuvida.buscarPorId(1L)).thenReturn(resposta);

        SolicitacaoDuvidaDetalhesResponse resultado = solicitacaoDuvidaService.buscarPorId(1L);

        assertEquals(resposta, resultado);
        verify(readSolicitacaoDuvida).buscarPorId(1L);
    }

    @Test
    @DisplayName("aceitar deve delegar ao use case AceitarSolicitacao")
    void deveDelegarAceiteAoUseCase() {
        SolicitacaoAceiteRequest request = mock(SolicitacaoAceiteRequest.class);
        Usuario usuario = mock(Usuario.class);
        SolicitacaoClientePreenchimentoResponse resposta = mock(SolicitacaoClientePreenchimentoResponse.class);
        when(aceitarSolicitacao.aceitar(1L, request, usuario)).thenReturn(resposta);

        SolicitacaoClientePreenchimentoResponse resultado = solicitacaoDuvidaService.aceitar(1L, request, usuario);

        assertEquals(resposta, resultado);
        verify(aceitarSolicitacao).aceitar(1L, request, usuario);
    }

    @Test
    @DisplayName("negar deve delegar ao use case NegarSolicitacao")
    void deveDelegarNegacaoAoUseCase() {
        solicitacaoDuvidaService.negar(1L);

        verify(negarSolicitacao).negar(1L);
    }

    @Test
    @DisplayName("buscarDuplicidades deve delegar ao use case BuscarCandidatosDuplicidadeSolicitacao")
    void deveDelegarBuscaDuplicidadesAoUseCase() {
        List<SolicitacaoCandidatoDuplicidadeResponse> lista = List.of(mock(SolicitacaoCandidatoDuplicidadeResponse.class));
        when(buscarCandidatosDuplicidadeSolicitacao.buscarPorSolicitacaoId(1L)).thenReturn(lista);

        List<SolicitacaoCandidatoDuplicidadeResponse> resultado = solicitacaoDuvidaService.buscarDuplicidades(1L);

        assertEquals(lista, resultado);
        verify(buscarCandidatosDuplicidadeSolicitacao).buscarPorSolicitacaoId(1L);
    }

    @Test
    @DisplayName("filtrar deve delegar ao use case FiltrarSolicitacoes")
    void deveDelegarFiltragemAoUseCase() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SolicitacaoDuvidaListarResponse> pagina = new PageImpl<>(List.of(), pageable, 0);
        when(filtrarSolicitacoes.filtrar("SP", "maria", null, null, pageable)).thenReturn(pagina);

        Page<SolicitacaoDuvidaListarResponse> resultado =
                solicitacaoDuvidaService.filtrar("SP", "maria", null, null, pageable);

        assertEquals(pagina, resultado);
        verify(filtrarSolicitacoes).filtrar("SP", "maria", null, null, pageable);
    }
}
