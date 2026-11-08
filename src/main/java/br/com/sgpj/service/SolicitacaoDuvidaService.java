package br.com.sgpj.service;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.request.SolicitacaoAceiteRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoCandidatoDuplicidadeResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.dto.response.Processos.SincronizacaoProcessoResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.service.useCases.solicitacaoDuvida.AceitarSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.BuscarCandidatosDuplicidadeSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.CreateSolicitacaoDuvida;
import br.com.sgpj.service.useCases.solicitacaoDuvida.FiltrarSolicitacoes;
import br.com.sgpj.service.useCases.solicitacaoDuvida.ListarSolicitacoesPendentes;
import br.com.sgpj.service.useCases.solicitacaoDuvida.NegarSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.ReadSolicitacaoDuvida;
import br.com.sgpj.service.useCases.solicitacaoDuvida.SolicitarSincronizacaoSolicitacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SolicitacaoDuvidaService {

    private final CreateSolicitacaoDuvida createSolicitacaoDuvida;
    private final ListarSolicitacoesPendentes listarSolicitacoesPendentes;
    private final AceitarSolicitacao aceitarSolicitacao;
    private final NegarSolicitacao negarSolicitacao;
    private final ReadSolicitacaoDuvida readSolicitacaoDuvida;
    private final FiltrarSolicitacoes filtrarSolicitacoes;
    private final BuscarCandidatosDuplicidadeSolicitacao buscarCandidatosDuplicidadeSolicitacao;
    private final SolicitarSincronizacaoSolicitacao solicitarSincronizacaoSolicitacao;

    public SolicitacaoDuvidaService(CreateSolicitacaoDuvida createSolicitacaoDuvida,
                                     ListarSolicitacoesPendentes listarSolicitacoesPendentes,
                                     AceitarSolicitacao aceitarSolicitacao,
                                     NegarSolicitacao negarSolicitacao,
                                     ReadSolicitacaoDuvida readSolicitacaoDuvida,
                                     FiltrarSolicitacoes filtrarSolicitacoes,
                                     BuscarCandidatosDuplicidadeSolicitacao buscarCandidatosDuplicidadeSolicitacao,
                                     SolicitarSincronizacaoSolicitacao solicitarSincronizacaoSolicitacao) {
        this.createSolicitacaoDuvida = createSolicitacaoDuvida;
        this.listarSolicitacoesPendentes = listarSolicitacoesPendentes;
        this.aceitarSolicitacao = aceitarSolicitacao;
        this.negarSolicitacao = negarSolicitacao;
        this.readSolicitacaoDuvida = readSolicitacaoDuvida;
        this.filtrarSolicitacoes = filtrarSolicitacoes;
        this.buscarCandidatosDuplicidadeSolicitacao = buscarCandidatosDuplicidadeSolicitacao;
        this.solicitarSincronizacaoSolicitacao = solicitarSincronizacaoSolicitacao;
    }

    // CREATE OPERATIONS
    @Transactional
    public SolicitacaoDuvidaDetalhesResponse criar(SolicitacaoDuvidaCriacaoRequest request) {
        return createSolicitacaoDuvida.criarSolicitacao(request);
    }

    // READ OPERATIONS
    @Transactional(readOnly = true)
    public Page<SolicitacaoDuvidaListarResponse> listarPendentes(Pageable pageable) {
        return listarSolicitacoesPendentes.listarPendentes(pageable);
    }

    @Transactional(readOnly = true)
    public Page<SolicitacaoDuvidaListarResponse> filtrar(
            String estado,
            Boolean isPF,
            String pesquisa,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable) {
        return filtrarSolicitacoes.filtrar(estado, isPF, pesquisa, dataInicio, dataFim, pageable);
    }

    @Transactional(readOnly = true)
    public SolicitacaoDuvidaDetalhesResponse buscarPorId(Long id) {
        return readSolicitacaoDuvida.buscarPorId(id);
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoCandidatoDuplicidadeResponse> buscarDuplicidades(Long id) {
        return buscarCandidatosDuplicidadeSolicitacao.buscarPorSolicitacaoId(id);
    }

    // UPDATE OPERATIONS
    @Transactional
    public SolicitacaoClientePreenchimentoResponse aceitar(Long id, SolicitacaoAceiteRequest request, Usuario usuarioResponsavel) {
        return aceitarSolicitacao.aceitar(id, request, usuarioResponsavel);
    }

    @Transactional
    public void negar(Long id) {
        negarSolicitacao.negar(id);
    }

    @Transactional
    public SincronizacaoProcessoResponse solicitarSincronizacaoDataJud(Long id) {
        return solicitarSincronizacaoSolicitacao.solicitar(id);
    }
}
