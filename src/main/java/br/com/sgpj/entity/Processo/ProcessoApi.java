package br.com.sgpj.entity.Processo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "processo_dados_api")
public class ProcessoApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroCnj;
    private String tribunalTramitacao;
    private String instancia;
    private LocalDateTime dataAjuizamento;
    private Integer nivelSigilo;
    private String classeProcessual;
    private String formato;
    private String sistemaTribunal;
    private String orgaoJulgador;
    private String assunto;

    @OneToMany(mappedBy = "processoApi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovimentacoesOficiais> movimentacoes;


    public ProcessoApi() {
    }

    public ProcessoApi(Long id, String numeroCnj, String tribunalTramitacao, String instancia, LocalDateTime dataAjuizamento, Integer nivelSigilo, String classeProcessual, String formato, String sistemaTribunal, String orgaoJulgador, String assunto, List<MovimentacoesOficiais> movimentacoes) {
        this.id = id;
        this.numeroCnj = numeroCnj;
        this.tribunalTramitacao = tribunalTramitacao;
        this.instancia = instancia;
        this.dataAjuizamento = dataAjuizamento;
        this.nivelSigilo = nivelSigilo;
        this.classeProcessual = classeProcessual;
        this.formato = formato;
        this.sistemaTribunal = sistemaTribunal;
        this.orgaoJulgador = orgaoJulgador;
        this.assunto = assunto;
        this.movimentacoes = movimentacoes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroCnj() {
        return numeroCnj;
    }

    public void setNumeroCnj(String numeroCnj) {
        this.numeroCnj = numeroCnj;
    }

    public String getTribunalTramitacao() {
        return tribunalTramitacao;
    }

    public void setTribunalTramitacao(String tribunalTramitacao) {
        this.tribunalTramitacao = tribunalTramitacao;
    }

    public String getInstancia() {
        return instancia;
    }

    public void setInstancia(String instancia) {
        this.instancia = instancia;
    }

    public LocalDateTime getDataAjuizamento() {
        return dataAjuizamento;
    }

    public void setDataAjuizamento(LocalDateTime dataAjuizamento) {
        this.dataAjuizamento = dataAjuizamento;
    }

    public Integer getNivelSigilo() {
        return nivelSigilo;
    }

    public void setNivelSigilo(Integer nivelSigilo) {
        this.nivelSigilo = nivelSigilo;
    }

    public String getClasseProcessual() {
        return classeProcessual;
    }

    public void setClasseProcessual(String classeProcessual) {
        this.classeProcessual = classeProcessual;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getSistemaTribunal() {
        return sistemaTribunal;
    }

    public void setSistemaTribunal(String sistemaTribunal) {
        this.sistemaTribunal = sistemaTribunal;
    }

    public String getOrgaoJulgador() {
        return orgaoJulgador;
    }

    public void setOrgaoJulgador(String orgaoJulgador) {
        this.orgaoJulgador = orgaoJulgador;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public List<MovimentacoesOficiais> getMovimentacoes() {
        return movimentacoes;
    }

    public void setMovimentacoes(List<MovimentacoesOficiais> movimentacoes) {
        this.movimentacoes = movimentacoes;
    }
}
