package br.com.sgpj.service;

import br.com.sgpj.dto.response.Dashboard.AudienciasResponse;
import br.com.sgpj.dto.response.Dashboard.DadosIndependentesResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final ProcessoAppRepository processoAppRepository;
    private final ClienteRepository clienteRepository;


    public DashboardService(ProcessoAppRepository processoAppRepository, ClienteRepository clienteRepository) {
        this.processoAppRepository = processoAppRepository;
        this.clienteRepository = clienteRepository;
    }

    public DadosIndependentesResponse listarDadosIndependentes(){
        List<ProcessoApp> processos = processoAppRepository.findAll();
        List<Cliente> clientes = clienteRepository.findAll();

        List<ProcessoApp> solicitacoes = new ArrayList<>();
        List<AudienciasResponse> audiencia = new ArrayList<>();

        for (ProcessoApp processo : processos) {
            if (processo.getCliente() != null && processo.getCliente().getStatusCliente() != null
                    && "LEAD".equalsIgnoreCase(processo.getCliente().getStatusCliente().getNome())) {
                solicitacoes.add(processo);
            }

            if (processo.getDataAudiencia() != null && !processo.getDataAudiencia().isBefore(LocalDateTime.now())) {
                audiencia.add(new AudienciasResponse(processo.getId(), processo.getDataAudiencia(), processo.getFaseProcesso(), processo.getUsuarioResponsavel(), processo.getPrioridade(), processo.getCliente().getNomeRazaoSocial()));
            }
        }

        audiencia.sort(Comparator.comparing(AudienciasResponse::dataAudiencia));

        return new DadosIndependentesResponse(
                processos.size(),
                solicitacoes.size(),
                clientes.size(),
                audiencia.size(),
                audiencia
        );
    }


    // public Response buscar dados dashboard dependentes de filtro
}
