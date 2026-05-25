package br.com.sgpj.service;

import br.com.sgpj.adapter.database.UsuarioPersistenciaAdapter;
import br.com.sgpj.dto.request.HistoricoProcessoRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoCreateResponse;
import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.mapper.HistoricoProcessoMapper;
import br.com.sgpj.repository.FaseProcessoRepository;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.TipoAlteracaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HistoricoProcessoService {

    private final HistoricoProcessoRepository historicoProcessoRepository;
    private final UsuarioPersistenciaAdapter usuarioPersistenciaAdapter;
    private final ProcessoAppRepository processoAppRepository;
    private final TipoAlteracaoRepository tipoAlteracaoRepository;
    private final FaseProcessoRepository faseProcessoRepository;

    public HistoricoProcessoService(HistoricoProcessoRepository historicoProcessoRepository, UsuarioPersistenciaAdapter usuarioPersistenciaAdapter, ProcessoAppRepository processoAppRepository, TipoAlteracaoRepository tipoAlteracaoRepository, FaseProcessoRepository faseProcessoRepository) {
        this.historicoProcessoRepository = historicoProcessoRepository;
        this.usuarioPersistenciaAdapter = usuarioPersistenciaAdapter;
        this.processoAppRepository = processoAppRepository;
        this.tipoAlteracaoRepository = tipoAlteracaoRepository;
        this.faseProcessoRepository = faseProcessoRepository;
    }



    public HistoricoProcessoCreateResponse adicionarRegistro(HistoricoProcessoRequest registro){

        HistoricoProcesso novoRegistro = new HistoricoProcesso();
        novoRegistro.setProcessoApp(processoAppRepository.getReferenceById(registro.processoAppId()));
        novoRegistro.setUsuarioUtilizado(usuarioPersistenciaAdapter.buscarUsuarioPorId(registro.usuarioUtilizadoId()));
        novoRegistro.setDescricaoAlteracao(registro.descricaoAlteracao());
        novoRegistro.setDataHoraAlteracao(LocalDateTime.now());
        novoRegistro.setTipoAlteracao(tipoAlteracaoRepository.getReferenceById(registro.tipoAlteracaoId()));


        historicoProcessoRepository.save(novoRegistro);

        HistoricoProcessoCreateResponse registrado = HistoricoProcessoMapper.toCreateResponse(novoRegistro);

        return registrado;
    }

}
