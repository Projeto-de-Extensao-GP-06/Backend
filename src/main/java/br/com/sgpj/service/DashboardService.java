package br.com.sgpj.service;

import br.com.sgpj.dto.response.Dashboard.*;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class DashboardService {

    private final ProcessoAppRepository processoAppRepository;
    private final ClienteRepository clienteRepository;
    private final FaseProcessoRepository faseProcessoRepository;

    private List<ProcessoApp> processos = new ArrayList<>();
    private List<Cliente> clientes = new ArrayList<>();
    private List<FaseProcesso> fasesProcessos = new ArrayList<>();
    private final List<ProcessoApp> solicitacoes = new ArrayList<>();


    public void popularVariaveisEscopoDash(){
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
    public DadosIndependentesResponse listarDadosIndependentes(){
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

        return new DadosIndependentesResponse(
                processos.size(),
                solicitacoesPendentes,
                clientes.size(),
                audiencia.size(),
                processosPorFase,
                tempoDuracaoProcessos,
                audiencia,
                formasDeEntrada
        );
    }


    public DadosFiltradosDashResponse listarDadosFiltradosDash(LocalDate dateFrom, LocalDate dateTo){
        popularVariaveisEscopoDash();
        ProcessosEncerradosResponse processosEncerrados = extrairProcessosEncerrados(dateFrom, dateTo, processos);
        ProcessosSuspensosResponse processosSuspensos = extrairProcessosSuspensos(dateFrom, dateTo, processos);
        FormasDeEntradaResponse formasDeEntrada = extrairFormasDeEntrada(clientes);
        EvolucaoCarteiraResponse evolucaoCarteira = extrairEvolucaoCarteira(dateFrom, dateTo, processos);
        FunilAtendimentoResponse funilAtendimento = extrairFunilAtendimento(dateFrom, dateTo, solicitacoes);

        return new DadosFiltradosDashResponse(
                processosEncerrados,
                processosSuspensos,
                formasDeEntrada,
                evolucaoCarteira,
                funilAtendimento
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
                if (processo.getFaseProcesso() != null && processo.getFaseProcesso().equals(faseAtual)){
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
        int entradaPorInstagram = 0;
        int entradaPorSite = 0;
        int entradaPorOutros = 0;

        for (Cliente cliente : clientes){
            if (cliente.getComoConheceu() != null) {
                switch (cliente.getComoConheceu().toLowerCase()){
                    case "instagram" -> entradaPorInstagram++;
                    case "site" -> entradaPorSite++;
                    case "outros" -> entradaPorOutros++;
                    default -> {}
                }
            }
        }

        return new FormasDeEntradaResponse(entradaPorInstagram, entradaPorSite, entradaPorOutros);
    }



    /*GRÁFICOS COM FILTRO*/
    public EvolucaoCarteiraResponse extrairEvolucaoCarteira(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<ProcessoApp> processos
    ){
        List<ProcessoApp> processosAtivos = new ArrayList<>();
        List<ProcessoApp> processosEncerrados = new ArrayList<>();
        List<ProcessoApp> processosSuspensos = new ArrayList<>();
        List<ProcessoApp> processosNovos = new ArrayList<>();

        String[] justificativasEncerrados = {"ACORDO", "SEM RESOLUÇÃO DO MÉRITO", "PENHORA TOTAL", "PAGAMENTO VOLUNTÁRIO"};
        String[] justificativasSuspensos = {"ACORDO PARCELADO", "TRATATIVAS", "AUSÊNCIA DE BENS", "AUSÊNCIA DE BENSL", "DETERMINAÇÃO JUDICIAL"};

        for (ProcessoApp processo : processos){
            if (
                    processo.getDataHoraInicio() != null &&
                    processo.getDataHoraInicio().isAfter(dateFrom.atStartOfDay()) &&
                    processo.getDataHoraInicio().isBefore(dateTo.atStartOfDay())
            ) {
                processosNovos.add(processo);

                if (processo.getEncerradoSuspensoEm() == null){
                    processosAtivos.add(processo);
                }
                else if (processo.getJustificativa() != null &&
                        Arrays.stream(justificativasEncerrados)
                                .anyMatch(j -> j.equalsIgnoreCase(processo.getJustificativa()))) {
                    processosEncerrados.add(processo);
                }
                else if (processo.getJustificativa() != null &&
                        Arrays.stream(justificativasSuspensos)
                                .anyMatch(j -> j.equalsIgnoreCase(processo.getJustificativa()))) {
                    processosSuspensos.add(processo);
                }
            }
        }

        return new EvolucaoCarteiraResponse(
                processosAtivos.size(),
                processosEncerrados.size(),
                processosSuspensos.size(),
                processosNovos.size()
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
            if (
                    processo.getDataHoraInicio() != null &&
                    processo.getDataHoraInicio().isAfter(dateFrom.atStartOfDay()) &&
                    processo.getDataHoraInicio().isBefore(dateTo.atStartOfDay()) &&
                    processo.getEncerradoSuspensoEm() != null
            ){
                if (processo.getJustificativa() != null) {
                    switch (processo.getJustificativa()){
                        case "ACORDO" -> encerradosPorAcordo++;
                        case "SEM RESOLUÇÃO DO MÉRITO" -> encerradosPorSemResolucao++;
                        case "PENHORA TOTAL" -> encerradosPorPenhoraTotal++;
                        case "PAGAMENTO VOLUNTÁRIO" -> encerradosPorPagamentoVoluntario++;
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
            if (
                    processo.getDataHoraInicio() != null &&
                    processo.getDataHoraInicio().isAfter(dateFrom.atStartOfDay()) &&
                    processo.getDataHoraInicio().isBefore(dateTo.atStartOfDay()) &&
                    processo.getEncerradoSuspensoEm() != null
            ){
                if (processo.getJustificativa() != null) {
                    switch (processo.getJustificativa()){
                        case "ACORDO PARCELADO" -> suspensosPorAcordoParcelado++;
                        case "TRATATIVAS" -> suspensosPorTratativas++;
                        case "AUSÊNCIA DE BENS", "AUSÊNCIA DE BENSL" -> suspensosPorAusenciaDeBens++;
                        case "DETERMINAÇÃO JUDICIAL" -> suspensosPorDeterminacaoJudicial++;
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

    public FunilAtendimentoResponse extrairFunilAtendimento(
            LocalDate dateFrom,
            LocalDate dateTo,
            List<ProcessoApp> solicitacoes
    ){
        int recebidas = 0;
        int aceitas = 0;

        for (ProcessoApp solicitacao : solicitacoes){
            if (
                    solicitacao.getDataHoraInicio() != null &&
                    solicitacao.getDataHoraInicio().isAfter(dateFrom.atStartOfDay()) &&
                    solicitacao.getDataHoraInicio().isBefore(dateTo.atStartOfDay())
            ) {
                recebidas++;

                if (solicitacao.getStatus() == StatusProcesso.ATIVO ||
                        (solicitacao.getCliente() != null &&
                         solicitacao.getCliente().getStatusCliente() != null &&
                         "ATIVO".equalsIgnoreCase(solicitacao.getCliente().getStatusCliente().getNome()))) {
                    aceitas++;
                }
            }
        }

        return new FunilAtendimentoResponse(recebidas, aceitas);
    }
}
