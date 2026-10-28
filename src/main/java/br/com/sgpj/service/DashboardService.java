package br.com.sgpj.service;

import br.com.sgpj.dto.response.Dashboard.*;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class DashboardService {

    private final ProcessoAppRepository processoAppRepository;
    private final ClienteRepository clienteRepository;
    private final FaseProcessoRepository faseProcessoRepository;

    private List<ProcessoApp> processos = new ArrayList<>();
    private List<Cliente> clientes = new ArrayList<>();
    private List<FaseProcesso> fasesProcessos = new ArrayList<>();
    private final List<ProcessoApp> solicitacoes = new ArrayList<>();


    public synchronized void popularVariaveisEscopoDash(){
        processos = processoAppRepository.findAll();
        clientes = clienteRepository.findAll();
        fasesProcessos = faseProcessoRepository.findAll();

        solicitacoes.clear();
        for (ProcessoApp processo : processos) {
            if ((processo.getObservacao() != null && processo.getObservacao().startsWith("Solicitação enviada pelo formulário público"))
                    || processo.getStatus() == StatusProcesso.SOLICITACAO
                    || (processo.getCliente() != null && processo.getCliente().getStatusCliente() != null
                    && "LEAD".equalsIgnoreCase(processo.getCliente().getStatusCliente().getNome()))) {
                solicitacoes.add(processo);
            }
        }
    }

    public DashboardService(
            ProcessoAppRepository processoAppRepository,
            ClienteRepository clienteRepository,
            FaseProcessoRepository faseProcessoRepository
    ) {
        this.processoAppRepository = processoAppRepository;
        this.clienteRepository = clienteRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        popularVariaveisEscopoDash();
    }

    /*----------BUSCA DOS DADOS DOS GRÁFICOS PARA O CLIENT----------*/
    public synchronized DadosIndependentesResponse listarDadosIndependentes(){
        popularVariaveisEscopoDash();
        List<ProcessosPorFaseResponse> processosPorFase = extrairProcessosPorFase(processos, fasesProcessos);
        List<AudienciasResponse> audiencia = extrairAudiencias(processos);
        TempoDuracaoProcessosResponse tempoDuracaoProcessos = extrairTempoDuracaoProcessos(processos);
        FormasDeEntradaResponse formasDeEntrada = extrairFormasDeEntrada(clientes);

        int solicitacoesPendentes = (int) solicitacoes.stream()
                .filter(s -> (s.getCliente() != null && s.getCliente().getStatusCliente() != null
                        && "LEAD".equalsIgnoreCase(s.getCliente().getStatusCliente().getNome()))
                        || s.getStatus() == StatusProcesso.SOLICITACAO)
                .count();

        int processosAtivos = (int) processos.stream()
                .filter(p -> p.getStatus() == StatusProcesso.ATIVO)
                .count();

        int clientesAtivos = (int) clientes.stream()
                .filter(c -> c.getStatusCliente() != null && "ATIVO".equalsIgnoreCase(c.getStatusCliente().getNome()))
                .count();

        return new DadosIndependentesResponse(
                processosAtivos,
                solicitacoesPendentes,
                clientesAtivos,
                audiencia.size(),
                processosPorFase,
                tempoDuracaoProcessos,
                audiencia,
                formasDeEntrada
        );
    }


    public synchronized DadosFiltradosDashResponse listarDadosFiltradosDash(LocalDate dateFrom, LocalDate dateTo){
        popularVariaveisEscopoDash();
        ProcessosEncerradosResponse processosEncerrados = extrairProcessosEncerrados(dateFrom, dateTo, processos);
        ProcessosSuspensosResponse processosSuspensos = extrairProcessosSuspensos(dateFrom, dateTo, processos);
        FormasDeEntradaResponse formasDeEntrada = extrairFormasDeEntrada(dateFrom, dateTo, clientes);
        EvolucaoCarteiraResponse evolucaoCarteira = extrairEvolucaoCarteira(dateFrom, dateTo, processos);

        return new DadosFiltradosDashResponse(
                processosEncerrados,
                processosSuspensos,
                formasDeEntrada,
                evolucaoCarteira
        );
    }




    /*----------EXTRATORES DE INFO DOS GRÁFICOS----------*/
    /*GRÁFICOS SEM FILTRO*/
    public List<ProcessosPorFaseResponse> extrairProcessosPorFase(
            List<ProcessoApp> processos,
            List<FaseProcesso> fasesProcessos
    ){
        List<ProcessosPorFaseResponse> processosPorFase = new ArrayList<>();

        for (FaseProcesso faseAtual : fasesProcessos) {
            int qtdProcessoFaseAtual = 0;

            for (ProcessoApp processo : processos) {
                if (processo.getFaseProcesso() != null &&
                        (processo.getFaseProcesso().equals(faseAtual) ||
                         (faseAtual.getId() != null && Objects.equals(processo.getFaseProcesso().getId(), faseAtual.getId())))) {
                    qtdProcessoFaseAtual++;
                }
            }

            processosPorFase.add(new ProcessosPorFaseResponse(faseAtual.getNome(), qtdProcessoFaseAtual));
        }

        return processosPorFase;
    }


    public List<AudienciasResponse> extrairAudiencias(List<ProcessoApp> processos){

        List<AudienciasResponse> audiencia = new ArrayList<>();

        for (ProcessoApp processo : processos) {
            if (
                    processo.getDataAudiencia() != null &&
                    !processo.getDataAudiencia().isBefore(LocalDateTime.now())
            ) {
                audiencia.add(new AudienciasResponse(
                        processo.getId(),
                        processo.getDataAudiencia(),
                        processo.getFaseProcesso() != null ? processo.getFaseProcesso().getNome() : null,
                        processo.getUsuarioResponsavel() != null ? processo.getUsuarioResponsavel().getNome() : null,
                        processo.getUsuarioResponsavel() != null ? processo.getUsuarioResponsavel().getEmail() : null,
                        processo.getPrioridade() != null ? processo.getPrioridade().getNome() : null,
                        processo.getCliente() != null ? processo.getCliente().getNomeRazaoSocial() : null
                        )
                );
            }
        }
        audiencia.sort(Comparator.comparing(AudienciasResponse::dataAudiencia));
        return audiencia;
    }


    public TempoDuracaoProcessosResponse extrairTempoDuracaoProcessos(List<ProcessoApp> processos){

        int ate6Meses = 0;
        int de6a12Meses = 0;
        int de1a2Anos = 0;
        int de2a5Anos = 0;
        int mais5Anos = 0;

        for (ProcessoApp processo : processos) {
            if (processo.getDataHoraInicio() == null) continue;

            long meses = ChronoUnit.MONTHS.between(processo.getDataHoraInicio(), LocalDateTime.now());

            if (meses < 6) {
                ate6Meses++;
            } else if (meses < 12) {
                de6a12Meses++;
            } else if (meses < 24) {
                de1a2Anos++;
            } else if (meses < 60) {
                de2a5Anos++;
            } else {
                mais5Anos++;
            }
        }

        return new TempoDuracaoProcessosResponse(ate6Meses, de6a12Meses, de1a2Anos, de2a5Anos, mais5Anos);
    }

    public FormasDeEntradaResponse extrairFormasDeEntrada(
            List<Cliente> clientes
    ){
        return extrairFormasDeEntrada(null, null, clientes);
    }

    public FormasDeEntradaResponse extrairFormasDeEntrada(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<Cliente> clientes
    ){
        int entradaPorInstagram = 0;
        int entradaPorSite = 0;
        int entradaPorIndicacao = 0;
        int entradaPorOutros = 0;

        for (Cliente cliente : clientes){
            if (dateFrom != null && dateTo != null) {
                LocalDateTime dataReferencia = cliente.getDataHoraEnvioFormulario() != null
                        ? cliente.getDataHoraEnvioFormulario()
                        : cliente.getDataHoraTransformacaoEmCliente();
                if (!estaNoPeriodo(dataReferencia, dateFrom, dateTo)) {
                    continue;
                }
            }

            if (cliente.getComoConheceu() != null) {
                switch (cliente.getComoConheceu()){
                    case ComoConheceuEnum.INSTAGRAM -> entradaPorInstagram++;
                    case ComoConheceuEnum.SITE -> entradaPorSite++;
                    case ComoConheceuEnum.INDICACAO -> entradaPorIndicacao++;
                    case ComoConheceuEnum.OUTROS -> entradaPorOutros++;
                    default -> {}
                }
            }
        }

        return new FormasDeEntradaResponse(entradaPorInstagram, entradaPorSite, entradaPorIndicacao, entradaPorOutros);
    }



    /*GRÁFICOS COM FILTRO*/
    public EvolucaoCarteiraResponse extrairEvolucaoCarteira(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<ProcessoApp> processos
    ){
        int processosAtivos = 0;
        int processosEncerrados = 0;
        int processosSuspensos = 0;
        int processosNovos = 0;

        for (ProcessoApp processo : processos){
            if (!estaNoPeriodo(processo.getDataHoraInicio(), dateFrom, dateTo)) {
                continue;
            }

            if (processo.getStatus() != StatusProcesso.SOLICITACAO && processo.getStatus() != StatusProcesso.NEGADO) {
                processosNovos++;
            }

            if (processo.getStatus() == StatusProcesso.ATIVO) {
                processosAtivos++;
            } else if (processo.getStatus() == StatusProcesso.ENCERRADO) {
                processosEncerrados++;
            } else if (processo.getStatus() == StatusProcesso.SUSPENSO) {
                processosSuspensos++;
            }
        }

        return new EvolucaoCarteiraResponse(
                processosAtivos,
                processosEncerrados,
                processosSuspensos,
                processosNovos
        );
    }

    public ProcessosEncerradosResponse extrairProcessosEncerrados(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<ProcessoApp> processos
    ) {

        int encerradosPorAcordo = 0;
        int encerradosPorSemResolucao = 0;
        int encerradosPorPenhoraTotal = 0;
        int encerradosPorPagamentoVoluntario = 0;
        int encerradosPorOutros = 0;

        for (ProcessoApp processo : processos){
            if (processo.getStatus() != StatusProcesso.ENCERRADO) {
                continue;
            }

            if (estaNoPeriodo(processo.getDataHoraInicio(), dateFrom, dateTo)) {
                String motivo = obterMotivo(processo);
                if (motivo != null) {
                    switch (motivo) {
                        case "ACORDO" -> encerradosPorAcordo++;
                        case "SEM RESOLUÇÃO DO MÉRITO", "SEM RESOLUCAO DO MERITO" -> encerradosPorSemResolucao++;
                        case "PENHORA TOTAL" -> encerradosPorPenhoraTotal++;
                        case "PAGAMENTO VOLUNTÁRIO", "PAGAMENTO VOLUNTARIO" -> encerradosPorPagamentoVoluntario++;
                        default -> encerradosPorOutros++;
                    }
                } else {
                    encerradosPorOutros++;
                }
            }
        }

        return new ProcessosEncerradosResponse(
                encerradosPorAcordo,
                encerradosPorSemResolucao,
                encerradosPorPenhoraTotal,
                encerradosPorPagamentoVoluntario,
                encerradosPorOutros
        );
    }

    public ProcessosSuspensosResponse extrairProcessosSuspensos(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<ProcessoApp> processos
    ){

        int suspensosPorAcordoParcelado = 0;
        int suspensosPorTratativas = 0;
        int suspensosPorAusenciaDeBens = 0;
        int suspensosPorDeterminacaoJudicial = 0;
        int suspensosPorOutros = 0;

        for (ProcessoApp processo : processos){
            if (processo.getStatus() != StatusProcesso.SUSPENSO) {
                continue;
            }

            if (estaNoPeriodo(processo.getDataHoraInicio(), dateFrom, dateTo)) {
                String motivo = obterMotivo(processo);
                if (motivo != null) {
                    switch (motivo) {
                        case "ACORDO PARCELADO" -> suspensosPorAcordoParcelado++;
                        case "TRATATIVAS" -> suspensosPorTratativas++;
                        case "AUSÊNCIA DE BENS", "AUSENCIA DE BENS", "AUSÊNCIA DE BENSL" -> suspensosPorAusenciaDeBens++;
                        case "DETERMINAÇÃO JUDICIAL", "DETERMINACAO JUDICIAL" -> suspensosPorDeterminacaoJudicial++;
                        default -> suspensosPorOutros++;
                    }
                } else {
                    suspensosPorOutros++;
                }
            }
        }

        return new ProcessosSuspensosResponse(
                suspensosPorAcordoParcelado,
                suspensosPorTratativas,
                suspensosPorAusenciaDeBens,
                suspensosPorDeterminacaoJudicial,
                suspensosPorOutros
        );
    }

    private boolean estaNoPeriodo(LocalDateTime dataHora, LocalDate dateFrom, LocalDate dateTo) {
        if (dataHora == null || dateFrom == null || dateTo == null) {
            return false;
        }
        LocalDate data = dataHora.toLocalDate();
        return !data.isBefore(dateFrom) && !data.isAfter(dateTo);
    }

    private String obterMotivo(ProcessoApp processo) {
        if (processo.getMotivoStatus() != null && processo.getMotivoStatus().getNome() != null) {
            return processo.getMotivoStatus().getNome().trim().toUpperCase();
        }
        if (processo.getJustificativa() != null && !processo.getJustificativa().isBlank()) {
            return processo.getJustificativa().trim().toUpperCase();
        }
        return null;
    }
}
