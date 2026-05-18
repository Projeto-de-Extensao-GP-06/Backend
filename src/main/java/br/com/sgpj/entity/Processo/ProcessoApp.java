package br.com.sgpj.entity.Processo;


import br.com.sgpj.entity.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processo_dados_aplicacao")
public class ProcessoApp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    private String descricaoCaso;

    @ManyToOne
    @JoinColumn(name = "prioridade_id")
    private Prioridade prioridade;

    @ManyToOne
    @JoinColumn(name = "fase_processo_id")
    private FaseProcesso faseProcesso;
    private LocalDateTime ultimaAtualizacao;
    private LocalDateTime dataHoraInicio;
    private Boolean ativo;

    private LocalDateTime dataAudiencia;

    private LocalDateTime dataHoraEncerramento;
    @ManyToOne
    @JoinColumn(name = "encerrado_por_usuario_id")
    private Usuario encerradoPorUsuario;

    @ManyToOne
    @JoinColumn(name = "sigla_tribunal_id")
    private SiglaTribunal siglaTribunal;
    private Double valorCausa;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "processos_dados_api_id")
    private ProcessoApi processoApi;


    public ProcessoApp(){}


    public ProcessoApp(Prioridade prioridade, Long id, String descricaoCaso, Usuario usuarioResponsavel, FaseProcesso faseProcesso, LocalDateTime ultimaAtualizacao, LocalDateTime dataHoraInicio, Boolean ativo, LocalDateTime dataAudiencia, SiglaTribunal siglaTribunal, Double valorCausa) {
        this.id = id;
        this.prioridade = prioridade;
        this.descricaoCaso = descricaoCaso;
        this.usuarioResponsavel = usuarioResponsavel;
        this.faseProcesso = faseProcesso;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.dataHoraInicio = dataHoraInicio;
        this.ativo = ativo;
        this.dataAudiencia = dataAudiencia;
        this.siglaTribunal = siglaTribunal;
        this.valorCausa = valorCausa;
    }

    public ProcessoApp(Long id, Usuario usuarioResponsavel, String descricaoCaso, Prioridade prioridade, FaseProcesso faseProcesso, LocalDateTime ultimaAtualizacao, LocalDateTime dataHoraInicio, Boolean ativo, LocalDateTime dataAudiencia, SiglaTribunal siglaTribunal, ProcessoApi processoApi, Double valorCausa) {
        this.id = id;
        this.usuarioResponsavel = usuarioResponsavel;
        this.descricaoCaso = descricaoCaso;
        this.prioridade = prioridade;
        this.faseProcesso = faseProcesso;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.dataHoraInicio = dataHoraInicio;
        this.ativo = ativo;
        this.dataAudiencia = dataAudiencia;
        this.siglaTribunal = siglaTribunal;
        this.processoApi = processoApi;
        this.valorCausa = valorCausa;
    }

    public ProcessoApp(Long id, Usuario usuarioResponsavel, String descricaoCaso, Prioridade prioridade, FaseProcesso faseProcesso, LocalDateTime ultimaAtualizacao, LocalDateTime dataHoraInicio, Boolean ativo, SiglaTribunal siglaTribunal, ProcessoApi processoApi, Double valorCausa) {
        this.id = id;
        this.usuarioResponsavel = usuarioResponsavel;
        this.descricaoCaso = descricaoCaso;
        this.prioridade = prioridade;
        this.faseProcesso = faseProcesso;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.dataHoraInicio = dataHoraInicio;
        this.ativo = ativo;
        this.siglaTribunal = siglaTribunal;
        this.processoApi = processoApi;
        this.valorCausa = valorCausa;
    }

    public String getDescricaoCaso() {return descricaoCaso;}
    public Double getValorCausa() {return valorCausa;}
    public Long getId() {return id;}
    public LocalDateTime getDataAudiencia() {return dataAudiencia;}
    public Usuario getUsuarioResponsavel() {return usuarioResponsavel;}
    public Prioridade getPrioridade() {return prioridade;}
    public FaseProcesso getFaseProcesso() {return faseProcesso;}
    public LocalDateTime getUltimaAtualizacao() {return ultimaAtualizacao;}
    public LocalDateTime getDataHoraInicio() {return dataHoraInicio;}
    public Boolean getAtivo() {return ativo;}
    public LocalDateTime getDataHoraEncerramento() {return dataHoraEncerramento;}
    public Usuario getEncerradoPorUsuario() {return encerradoPorUsuario;}
    public SiglaTribunal getSiglaTribunal() {return siglaTribunal;}
    public ProcessoApi getProcessoApi() {return processoApi;}

    public void setDescricaoCaso(String descricaoCaso) {this.descricaoCaso = descricaoCaso;}
    public void setValorCausa(Double valorCausa) {this.valorCausa = valorCausa;}
    public void setProcessoApi(ProcessoApi processoApi) {this.processoApi = processoApi;}
    public void setDataAudiencia(LocalDateTime dataAudiencia) {this.dataAudiencia = dataAudiencia;}
    public void setUsuarioResponsavel(Usuario usuarioResponsavel) {this.usuarioResponsavel = usuarioResponsavel;}
    public void setId(Long id) {this.id = id;}
    public void setPrioridade(Prioridade prioridade) {this.prioridade = prioridade;}
    public void setFaseProcesso(FaseProcesso faseProcesso) {this.faseProcesso = faseProcesso;}
    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {this.ultimaAtualizacao = ultimaAtualizacao;}
    public void setDataHoraInicio(LocalDateTime dataHoraInicio) {this.dataHoraInicio = dataHoraInicio;}
    public void setAtivo(Boolean ativo) {this.ativo = ativo;}
    public void setDataHoraEncerramento(LocalDateTime dataHoraEncerramento) {this.dataHoraEncerramento = dataHoraEncerramento;}
    public void setEncerradoPorUsuario(Usuario encerradoPorUsuario) {this.encerradoPorUsuario = encerradoPorUsuario;}
    public void setSiglaTribunal(SiglaTribunal siglaTribunal) {this.siglaTribunal = siglaTribunal;}
}
