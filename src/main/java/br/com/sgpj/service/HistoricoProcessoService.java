package br.com.sgpj.service;

import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.mapper.HistoricoProcessoMapper;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.TipoAlteracaoRepository;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class HistoricoProcessoService {

    private final HistoricoProcessoRepository historicoProcessoRepository;
    private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
    private final ProcessoAppRepository processoAppRepository;
    private final TipoAlteracaoRepository tipoAlteracaoRepository;

    public HistoricoProcessoService(HistoricoProcessoRepository historicoProcessoRepository, UsuarioPersistenciaAdapter usuarioPersistenciaAdapter, ProcessoAppRepository processoAppRepository, TipoAlteracaoRepository tipoAlteracaoRepository, FaseProcessoRepository faseProcessoRepository) {
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
        this.processoAppRepository = processoAppRepository;
        this.tipoAlteracaoRepository = tipoAlteracaoRepository;
    }



    public HistoricoProcessoResponse adicionarRegistro(HistoricoProcessoRequest registro){

        HistoricoProcesso novoRegistro = new HistoricoProcesso();
        novoRegistro.setProcessoApp(processoAppRepository.findById(registro.processoAppId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo nÃ£o encontrado para o ID: " + registro.processoAppId())));
        novoRegistro.setUsuarioUtilizado(usuarioPersistenciaAdapter.buscarUsuarioPorId(registro.usuarioUtilizadoId()));
        novoRegistro.setDescricaoAlteracao(registro.descricaoAlteracao());
        novoRegistro.setDataHoraAlteracao(LocalDateTime.now());
        novoRegistro.setTipoAlteracao(tipoAlteracaoRepository.findById(registro.tipoAlteracaoId())
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Tipo de alteraÃ§Ã£o nÃ£o encontrado para o ID: " + registro.tipoAlteracaoId())));

        historicoProcessoRepository.save(novoRegistro);

        HistoricoProcessoResponse registrado = HistoricoProcessoMapper.toResponse(novoRegistro);

        return registrado;
    }

    public List<HistoricoProcessoResponse> buscarPorIdProcesso(Long id){
        ProcessoApp processoApp = processoAppRepository.findById(id)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Processo nÃ£o encontrado para o ID: " + id));

        List<HistoricoProcesso> historicoProcesso = historicoProcessoRepository.findByProcessoApp(processoApp);
        List<HistoricoProcessoResponse> historicoResponse = new ArrayList<>();

        for (HistoricoProcesso hist : historicoProcesso){
            historicoResponse.add(HistoricoProcessoMapper.toResponse(hist));
        }

        return historicoResponse;
    }

    public void deletarPorIdProcesso(Long id){
        Optional<ProcessoApp> processoApp = processoAppRepository.findById(id);

        if(processoApp.isEmpty()){
            throw new ObjetoNaoEncontradoException("Processo não encontrado para o ID: " + id);
        }

        historicoProcessoRepository.deleteByProcessoApp(processoApp.get());
    }

}
