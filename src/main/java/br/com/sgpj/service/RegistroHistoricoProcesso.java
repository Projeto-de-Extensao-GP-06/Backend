package br.com.sgpj.service;

import br.com.sgpj.entity.Processo.HistoricoProcesso;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.TipoAlteracao;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.ObjetoNaoEncontradoException;
import br.com.sgpj.repository.HistoricoProcessoRepository;
import br.com.sgpj.repository.TipoAlteracaoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegistroHistoricoProcesso {

    public static final String ABERTURA = "ABERTURA DE PROCESSO";
    public static final String ATUALIZACAO = "ATUALIZAÇÃO DE DADOS";
    public static final String STATUS = "MUDANÇA DE STATUS";
    public static final String DOCUMENTO = "ADIÇÃO DE DOCUMENTO";
    public static final String OBSERVACAO = "OBSERVAÇÃO";

    private final HistoricoProcessoRepository historicoRepository;
    private final TipoAlteracaoRepository tipoAlteracaoRepository;

    public RegistroHistoricoProcesso(HistoricoProcessoRepository historicoRepository,
                                     TipoAlteracaoRepository tipoAlteracaoRepository) {
        this.historicoRepository = historicoRepository;
        this.tipoAlteracaoRepository = tipoAlteracaoRepository;
    }

    public void registrar(ProcessoApp processo, Usuario usuario, String tipoNome, String descricao) {
        TipoAlteracao tipo = tipoAlteracaoRepository.findByNomeIgnoreCase(tipoNome)
                .orElseThrow(() -> new ObjetoNaoEncontradoException("Tipo de alteração não encontrado: " + tipoNome));

        HistoricoProcesso historico = new HistoricoProcesso();
        historico.setProcessoApp(processo);
        historico.setUsuarioUtilizado(usuario);
        historico.setTipoAlteracao(tipo);
        historico.setDescricaoAlteracao(descricao);
        historico.setDataHoraAlteracao(LocalDateTime.now());
        historicoRepository.save(historico);
    }
}
