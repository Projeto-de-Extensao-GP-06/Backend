package br.com.sgpj.entity.Processo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacao_oficial")
public class MovimentacaoOficial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String codigo;
    private String descricao;
    private LocalDateTime dataHoraMovimentacao;

    @ManyToOne
    @JoinColumn(name = "processo_dados_api_id")
    private ProcessoApi processoApi;

    public MovimentacaoOficial() {
    }

    public MovimentacaoOficial(Long id, String codigo, String descricao, LocalDateTime dataHoraMovimentacao, ProcessoApi processoApi) {
        this.id = id;
        this.codigo = codigo;
        this.descricao = descricao;
        this.dataHoraMovimentacao = dataHoraMovimentacao;
        this.processoApi = processoApi;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getDataHoraMovimentacao() {
        return dataHoraMovimentacao;
    }

    public void setDataHoraMovimentacao(LocalDateTime dataHoraMovimentacao) {
        this.dataHoraMovimentacao = dataHoraMovimentacao;
    }

    public ProcessoApi getProcessoApi() {
        return processoApi;
    }

    public void setProcessoApi(ProcessoApi processoApi) {
        this.processoApi = processoApi;
    }
}