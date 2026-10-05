package br.com.sgpj.entity;

import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_razao_social", nullable = false, length = 150)
    private String nomeRazaoSocial;

    @Column(name = "cpf_cnpj", length = 14)
    private String cpfCnpj;

    @Column(name = "is_pf", nullable = false)
    private Boolean isPF;

    @Column(length = 15)
    private String rg;

    @Enumerated(EnumType.STRING)
    @Column(name = "como_conheceu")
    private ComoConheceuEnum comoConheceu;

    @ManyToOne
    @JoinColumn(name = "status_cliente_id")
    private StatusCliente statusCliente;

    @ManyToOne
    @JoinColumn(name = "endereco_id")
    private Endereco endereco;

    @Column(name = "data_hora_envio_formulario")
    private LocalDateTime dataHoraEnvioFormulario;

    @Column(name = "data_hora_transformacao_em_cliente")
    private LocalDateTime dataHoraTransformacaoEmCliente;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<CaminhoPadraoDeArmazenamento> caminhos;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    private List<Contato> contatos;

    public Cliente() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeRazaoSocial() {
        return nomeRazaoSocial;
    }

    public void setNomeRazaoSocial(String nomeRazaoSocial) {
        this.nomeRazaoSocial = nomeRazaoSocial;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public StatusCliente getStatusCliente() {
        return statusCliente;
    }

    public void setStatusCliente(StatusCliente statusCliente) {
        this.statusCliente = statusCliente;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public LocalDateTime getDataHoraEnvioFormulario() {
        return dataHoraEnvioFormulario;
    }

    public void setDataHoraEnvioFormulario(LocalDateTime dataHoraEnvioFormulario) {
        this.dataHoraEnvioFormulario = dataHoraEnvioFormulario;
    }

    public LocalDateTime getDataHoraTransformacaoEmCliente() {
        return dataHoraTransformacaoEmCliente;
    }

    public void setDataHoraTransformacaoEmCliente(LocalDateTime dataHoraTransformacaoEmCliente) {
        this.dataHoraTransformacaoEmCliente = dataHoraTransformacaoEmCliente;
    }

    public List<Contato> getContatos() {
        return contatos;
    }

    public void setContatos(List<Contato> contatos) {
        this.contatos = contatos;
    }

    public List<CaminhoPadraoDeArmazenamento> getCaminhos() {
        return caminhos;
    }

    public void setCaminhos(List<CaminhoPadraoDeArmazenamento> caminhos) {
        this.caminhos = caminhos;
    }

    public Boolean getPF() { return isPF; }

    public void setPF(Boolean PF) { isPF = PF; }

    public ComoConheceuEnum getComoConheceu() { return comoConheceu; }

    public void setComoConheceu(ComoConheceuEnum comoConheceu) { this.comoConheceu = comoConheceu; }
}

