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
    private Usuario usuarioUtilizado;

    private String descricaoAlteracao;
    private LocalDateTime dataHoraAlteracao;

    @ManyToOne
    @JoinColumn(name = "tipo_alteracao_id")
    private TipoAlteracao tipoAlteracao;




    public HistoricoProcesso(Long id, ProcessoApp processoApp, Usuario usuarioUtilizado, String descricaoAlteracao, LocalDateTime dataHoraAlteracao, TipoAlteracao tipoAlteracao) {
        this.id = id;
        this.processoApp = processoApp;
        this.usuarioUtilizado = usuarioUtilizado;
        this.descricaoAlteracao = descricaoAlteracao;
        this.dataHoraAlteracao = dataHoraAlteracao;
        this.tipoAlteracao = tipoAlteracao;
    }

    public HistoricoProcesso() {
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

    public Usuario getUsuarioUtilizado() {
        return usuarioUtilizado;
    }

    public void setUsuarioUtilizado(Usuario usuarioUtilizado) {
        this.usuarioUtilizado = usuarioUtilizado;
    }

    public String getDescricaoAlteracao() {
        return descricaoAlteracao;
    }

    public void setDescricaoAlteracao(String DescricaoAlteracao) {
        this.descricaoAlteracao = DescricaoAlteracao;
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
}
