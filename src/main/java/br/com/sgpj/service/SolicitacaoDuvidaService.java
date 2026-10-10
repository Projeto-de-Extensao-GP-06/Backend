package br.com.sgpj.service;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.service.useCases.solicitacaoDuvida.AceitarSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.CreateSolicitacaoDuvida;
import br.com.sgpj.service.useCases.solicitacaoDuvida.FiltrarSolicitacoes;
import br.com.sgpj.service.useCases.solicitacaoDuvida.ListarSolicitacoesPendentes;
import br.com.sgpj.service.useCases.solicitacaoDuvida.NegarSolicitacao;
import br.com.sgpj.service.useCases.solicitacaoDuvida.ReadSolicitacaoDuvida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SolicitacaoDuvidaService {

    private final CreateSolicitacaoDuvida createSolicitacaoDuvida;
    private final ListarSolicitacoesPendentes listarSolicitacoesPendentes;
    private final AceitarSolicitacao aceitarSolicitacao;
    private final NegarSolicitacao negarSolicitacao;
    private final ReadSolicitacaoDuvida readSolicitacaoDuvida;
    private final FiltrarSolicitacoes filtrarSolicitacoes;

    public SolicitacaoDuvidaService(CreateSolicitacaoDuvida createSolicitacaoDuvida,
                                     ListarSolicitacoesPendentes listarSolicitacoesPendentes,
                                     AceitarSolicitacao aceitarSolicitacao,
                                      NegarSolicitacao negarSolicitacao,
                                      ReadSolicitacaoDuvida readSolicitacaoDuvida,
                                      FiltrarSolicitacoes filtrarSolicitacoes) {
        this.createSolicitacaoDuvida = createSolicitacaoDuvida;
        this.listarSolicitacoesPendentes = listarSolicitacoesPendentes;
        this.aceitarSolicitacao = aceitarSolicitacao;
        this.negarSolicitacao = negarSolicitacao;
        this.readSolicitacaoDuvida = readSolicitacaoDuvida;
        this.filtrarSolicitacoes = filtrarSolicitacoes;
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
            String pesquisa,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable) {
        return filtrarSolicitacoes.filtrar(estado, pesquisa, dataInicio, dataFim, pageable);
    }

    @Transactional(readOnly = true)
    public SolicitacaoDuvidaDetalhesResponse buscarPorId(Long id) {
        return readSolicitacaoDuvida.buscarPorId(id);
    }

    // UPDATE OPERATIONS
    @Transactional
    public SolicitacaoClientePreenchimentoResponse aceitar(Long id) {
        return aceitarSolicitacao.aceitar(id);
    }

    @Transactional
    public void negar(Long id) {
        negarSolicitacao.negar(id);
    }
}
