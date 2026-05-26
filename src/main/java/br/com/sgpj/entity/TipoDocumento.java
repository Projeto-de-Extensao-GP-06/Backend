package br.com.sgpj.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "tipo_documento")
public class TipoDocumento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @OneToMany(mappedBy = "tipoDocumento")
    private List<Documento> documentos;

    public TipoDocumento() {
    }

    public TipoDocumento(Long id, String nome, List<Documento> documentos) {
        this.id = id;
        this.nome = nome;
        this.documentos = documentos;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<Documento> documentos) {
        this.documentos = documentos;
    }
}
