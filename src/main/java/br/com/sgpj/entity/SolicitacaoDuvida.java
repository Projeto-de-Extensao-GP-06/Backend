package br.com.sgpj.entity;

import br.com.sgpj.entity.Processo.ProcessoApi;

import java.time.LocalDateTime;

public class SolicitacaoDuvida {

    private Long id;
    private Long clienteId;
    private String nomeCompleto;
    private String email;
    private String telefone;
    private String estado;
    private String numeroProcesso;
    private String descricao;
    private ComoConheceuEnum comoConheceu;
    private Boolean aceitouTermos;
    private LocalDateTime dataSolicitacao;
    private SolicitacaoStatus status;
    private LocalDateTime dataResposta;
    private ProcessoApi processoApi;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public ComoConheceuEnum getComoConheceu() {
        return comoConheceu;
    }

    public void setComoConheceu(ComoConheceuEnum comoConheceu) {
        this.comoConheceu = comoConheceu;
    }

    public Boolean getAceitouTermos() {
        return aceitouTermos;
    }

    public void setAceitouTermos(Boolean aceitouTermos) {
        this.aceitouTermos = aceitouTermos;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public SolicitacaoStatus getStatus() {
        return status;
    }

    public void setStatus(SolicitacaoStatus status) {
        this.status = status;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

    public ProcessoApi getProcessoApi() {
        return processoApi;
    }

    public void setProcessoApi(ProcessoApi processoApi) {
        this.processoApi = processoApi;
    }
}
