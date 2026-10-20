package br.com.sgpj.entity.Processo;


import br.com.sgpj.entity.Cliente;
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
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    @ManyToOne
    @JoinColumn(nullable = false, name = "cliente_id")
    private Cliente cliente;

    private String descricaoCaso;
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "prioridade_id")
    private Prioridade prioridade;

    @ManyToOne
    @JoinColumn(name = "fase_processo_id")
    private FaseProcesso faseProcesso;

    private LocalDateTime ultimaAtualizacao;
    private LocalDateTime dataHoraInicio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusProcesso status;

    private LocalDateTime dataAudiencia;

    @Column(name = "encerrado_suspenso_em")
    private LocalDateTime encerradoSuspensoEm;

    @ManyToOne
    @JoinColumn(name = "encerrado_suspenso_por_usuario_id")
    private Usuario encerradoSuspensoPorUsuario;

    @ManyToOne
    @JoinColumn(name = "motivo_status_processo_id")
    private MotivoStatusProcesso motivoStatus;

    @Column(name = "justificativa_status")
    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "sigla_tribunal_id")
    private SiglaTribunal siglaTribunal;

    private Double valorCausa;

    @Column(name = "cnpj_informado_formulario", length = 14)
    private String cnpjInformadoFormulario;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "processo_dados_api_id")
    private ProcessoApi processoApi;




    public ProcessoApp(){}


    public ProcessoApp(Prioridade prioridade, Long id, String descricaoCaso, String observacao, Usuario usuarioResponsavel, Cliente cliente, FaseProcesso faseProcesso, LocalDateTime ultimaAtualizacao, LocalDateTime dataHoraInicio, StatusProcesso status, LocalDateTime dataAudiencia, SiglaTribunal siglaTribunal, Double valorCausa) {
        this.id = id;
        this.prioridade = prioridade;
        this.descricaoCaso = descricaoCaso;
        this.observacao = observacao;
        this.usuarioResponsavel = usuarioResponsavel;
        this.cliente = cliente;
        this.faseProcesso = faseProcesso;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.dataHoraInicio = dataHoraInicio;
        this.status = status;
        this.dataAudiencia = dataAudiencia;
        this.siglaTribunal = siglaTribunal;
        this.valorCausa = valorCausa;
    }

    public ProcessoApp(Long id, Usuario usuarioResponsavel, String descricaoCaso, String observacao, Prioridade prioridade, FaseProcesso faseProcesso, LocalDateTime ultimaAtualizacao, LocalDateTime dataHoraInicio, StatusProcesso status, LocalDateTime dataAudiencia, SiglaTribunal siglaTribunal, ProcessoApi processoApi, Double valorCausa) {
        this.id = id;
        this.usuarioResponsavel = usuarioResponsavel;
        this.descricaoCaso = descricaoCaso;
        this.observacao = observacao;
        this.prioridade = prioridade;
        this.faseProcesso = faseProcesso;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.dataHoraInicio = dataHoraInicio;
        this.status = status;
        this.dataAudiencia = dataAudiencia;
        this.siglaTribunal = siglaTribunal;
        this.processoApi = processoApi;
        this.valorCausa = valorCausa;
    }

    public ProcessoApp(Long id, Usuario usuarioResponsavel, String descricaoCaso, String observacao, Prioridade prioridade, FaseProcesso faseProcesso, LocalDateTime ultimaAtualizacao, LocalDateTime dataHoraInicio, StatusProcesso status, SiglaTribunal siglaTribunal, ProcessoApi processoApi, Double valorCausa) {
        this.id = id;
        this.usuarioResponsavel = usuarioResponsavel;
        this.descricaoCaso = descricaoCaso;
        this.observacao = observacao;
        this.prioridade = prioridade;
        this.faseProcesso = faseProcesso;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.dataHoraInicio = dataHoraInicio;
        this.status = status;
        this.siglaTribunal = siglaTribunal;
        this.processoApi = processoApi;
        this.valorCausa = valorCausa;
    }

    public String getObservacao() {return observacao;}
    public Cliente getCliente() {return cliente;}
    public String getDescricaoCaso() {return descricaoCaso;}
    public Double getValorCausa() {return valorCausa;}
    public Long getId() {return id;}
    public LocalDateTime getDataAudiencia() {return dataAudiencia;}
    public Usuario getUsuarioResponsavel() {return usuarioResponsavel;}
    public Prioridade getPrioridade() {return prioridade;}
    public FaseProcesso getFaseProcesso() {return faseProcesso;}
    public LocalDateTime getUltimaAtualizacao() {return ultimaAtualizacao;}
    public LocalDateTime getDataHoraInicio() {return dataHoraInicio;}
    public StatusProcesso getStatus() {return status;}
    public LocalDateTime getEncerradoSuspensoEm() {return encerradoSuspensoEm;}
    public Usuario getEncerradoSuspensoPorUsuario() {return encerradoSuspensoPorUsuario;}
    public MotivoStatusProcesso getMotivoStatus() {return motivoStatus;}
    public String getJustificativa() {return justificativa;}
    public SiglaTribunal getSiglaTribunal() {return siglaTribunal;}
    public ProcessoApi getProcessoApi() {return processoApi;}
    public String getCnpjInformadoFormulario() {return cnpjInformadoFormulario;}

    public void setObservacao(String observacao) {this.observacao = observacao;}
    public void setCliente(Cliente cliente) {this.cliente = cliente;}
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
    public void setStatus(StatusProcesso status) {this.status = status;}
    public void setEncerradoSuspensoEm(LocalDateTime encerradoSuspensoEm) {this.encerradoSuspensoEm = encerradoSuspensoEm;}
    public void setEncerradoSuspensoPorUsuario(Usuario encerradoSuspensoPorUsuario) {this.encerradoSuspensoPorUsuario = encerradoSuspensoPorUsuario;}
    public void setMotivoStatus(MotivoStatusProcesso motivoStatus) {this.motivoStatus = motivoStatus;}
    public void setJustificativa(String justificativa) {this.justificativa = justificativa;}
    public void setSiglaTribunal(SiglaTribunal siglaTribunal) {this.siglaTribunal = siglaTribunal;}
    public void setCnpjInformadoFormulario(String cnpjInformadoFormulario) {this.cnpjInformadoFormulario = cnpjInformadoFormulario;}
}
