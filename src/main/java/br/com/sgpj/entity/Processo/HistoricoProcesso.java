package br.com.sgpj.entity.Processo;

import br.com.sgpj.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_processo")
public class HistoricoProcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "processo_dados_aplicacao_id")
    private ProcessoApp processoApp;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioUsado;

    private String descricao;
    private LocalDateTime dataHoraAlteracao;

    @ManyToOne
    @JoinColumn(name = "tipo_alteracao_id")
    private TipoAlteracao tipoAlteracao;

    @ManyToOne
    @JoinColumn(name = "fase_anterior_id")
    private FaseProcesso faseAnterior;

    @ManyToOne
    @JoinColumn(name = "fase_nova_id")
    private FaseProcesso faseNova;


    public HistoricoProcesso(Long id, ProcessoApp processoApp, Usuario usuarioUsado, String descricao, LocalDateTime dataHoraAlteracao, TipoAlteracao tipoAlteracao, FaseProcesso faseAnterior, FaseProcesso faseNova) {
        this.id = id;
        this.processoApp = processoApp;
        this.usuarioUsado = usuarioUsado;
        this.descricao = descricao;
        this.dataHoraAlteracao = dataHoraAlteracao;
        this.tipoAlteracao = tipoAlteracao;
        this.faseAnterior = faseAnterior;
        this.faseNova = faseNova;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProcessoApp getProcessoApp() {
        return processoApp;
    }

    public void setProcessoApp(ProcessoApp processoApp) {
        this.processoApp = processoApp;
    }

    public Usuario getUsuarioUsado() {
        return usuarioUsado;
    }

    public void setUsuarioUsado(Usuario usuarioUsado) {
        this.usuarioUsado = usuarioUsado;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataHoraAlteracao() {
        return dataHoraAlteracao;
    }

    public void setDataHoraAlteracao(LocalDateTime dataHoraAlteracao) {
        this.dataHoraAlteracao = dataHoraAlteracao;
    }

    public TipoAlteracao getTipoAlteracao() {
        return tipoAlteracao;
    }

    public void setTipoAlteracao(TipoAlteracao tipoAlteracao) {
        this.tipoAlteracao = tipoAlteracao;
    }

    public FaseProcesso getFaseAnterior() {
        return faseAnterior;
    }

    public void setFaseAnterior(FaseProcesso faseAnterior) {
        this.faseAnterior = faseAnterior;
    }

    public FaseProcesso getFaseNova() {
        return faseNova;
    }

    public void setFaseNova(FaseProcesso faseNova) {
        this.faseNova = faseNova;
    }
}
