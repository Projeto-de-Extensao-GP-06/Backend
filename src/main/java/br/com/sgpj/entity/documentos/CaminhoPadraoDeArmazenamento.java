package br.com.sgpj.entity.documentos;

import br.com.sgpj.entity.Cliente;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "padrao_caminho_armazenamento")
public class CaminhoPadraoDeArmazenamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonBackReference
    private Cliente cliente;

    @Column(name = "caminho_base", nullable = false, unique = true)
    private String caminho;

    @OneToMany(mappedBy = "padraoArmazenamento")
    private List<Documento> documentos;

    public CaminhoPadraoDeArmazenamento() {
    }

    public CaminhoPadraoDeArmazenamento(Long id, Cliente cliente, String caminho, List<Documento> documentos) {
        this.id = id;
        this.cliente = cliente;
        this.caminho = caminho;
        this.documentos = documentos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }
}
