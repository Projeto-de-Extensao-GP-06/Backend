package br.com.sgpj.service;

import br.com.sgpj.adapter.api.DataJudAdapter;
import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.response.ProcessoAppResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.ProcessoApiMapper;
import br.com.sgpj.mapper.ProcessoAppMapper;
import br.com.sgpj.repository.*;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class ProcessoService {
    private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
    private final DataJudAdapter dataJudAdapter;

    private final ProcessoAppRepository processoAppRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final FaseProcessoRepository faseProcessoRepository;
    private final SiglaTribunalRepository siglaTribunalRepository;
    private final ClienteRepository clienteRepository;

    public ProcessoService(
            UsuarioPersistenciaAdapter usuarioPersistenciaAdapter,
            DataJudAdapter dataJudAdapter,
            ProcessoAppRepository processoAppRepository,
            PrioridadeRepository prioridadeRepository,
            FaseProcessoRepository faseProcessoRepository,
            SiglaTribunalRepository siglaTribunalRepository, ClienteRepository clienteRepository) {
        this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
        this.dataJudAdapter = dataJudAdapter;
        this.processoAppRepository = processoAppRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.faseProcessoRepository = faseProcessoRepository;
        this.siglaTribunalRepository = siglaTribunalRepository;
        this.clienteRepository = clienteRepository;
    }

    public ProcessoAppResponse criarProcesso(ProcessoAppCriacaoRequest novoProcesso) {
        Usuario usuarioResponsavel = usuarioPersistenciaAdapter.buscarUsuarioPorId(novoProcesso.usuarioResponsavelId());
        Prioridade prioridade = prioridadeRepository.findById(novoProcesso.prioridadeId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Prioridade nao encontrada para o ID: " + novoProcesso.prioridadeId()));
        FaseProcesso faseProcesso = faseProcessoRepository.findById(novoProcesso.faseProcessoId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Fase do processo nao encontrada para o ID: " + novoProcesso.faseProcessoId()));
        SiglaTribunal siglaTribunal = siglaTribunalRepository.findById(novoProcesso.siglaTribunalId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Sigla do tribunal nao encontrada para o ID: " + novoProcesso.siglaTribunalId()));
        Cliente cliente = clienteRepository.findById(novoProcesso.clienteId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Cliente nao encontrada para o ID: " + novoProcesso.clienteId()));

        ProcessoApi processoApi = buscarDadosOficiais(novoProcesso.numeroProcesso(), siglaTribunal);
        ProcessoApp processoApp = ProcessoAppMapper.toCreateEntity(novoProcesso);

        processoApp.setUsuarioResponsavel(usuarioResponsavel);
        processoApp.setCliente(cliente);
        processoApp.setPrioridade(prioridade);
        processoApp.setFaseProcesso(faseProcesso);
        processoApp.setSiglaTribunal(siglaTribunal);
        processoApp.setProcessoApi(processoApi);

        return ProcessoAppMapper.toResponse(processoAppRepository.save(processoApp));
    }

    public ProcessoApi buscarDadosOficiais(String numeroProcesso, SiglaTribunal siglaTribunal) {
        if (numeroProcesso == null || numeroProcesso.isBlank()) {
            return null;
        }

        String dataJudResponse = dataJudAdapter.buscarPorNumeroDeProcesso(numeroProcesso, siglaTribunal.getNome());
        JSONObject jsonDataJud = new JSONObject(dataJudResponse);

        return ProcessoApiMapper.toEntity(jsonDataJud);
    }
}
