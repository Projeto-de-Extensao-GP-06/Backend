package br.com.sgpj.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_original")
    private String nomeOriginal;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "tamanho_bytes")
    private Long tamanhoBytes;

    @Column(name = "onedrive_item_id")
    private String onedriveItemId;

    @Column(name = "onedrive_url_thumbnail")
    private String onedriveUrlThumbnail;

    @Column(name = "onedrive_url_download")
    private String onedriveUrlDownload;

    @Column(name = "data_hora_criacao")
    private LocalDateTime dataHoraCriacao;

    @Column(name = "ultima_atualizacao")
    private LocalDateTime ultimaAtualizacao;

    @Column(name = "ativo")
    private Boolean ativo;

    @Column(name = "inativado_em")
    private LocalDateTime inativadoEm;


    @ManyToOne
    @JoinColumn(name = "criado_por_usuario_id")
    @JsonBackReference
    private Usuario criadoPorUsuario;

    @ManyToOne
    @JoinColumn(name = "tipo_documento_id")
    @JsonBackReference
    private TipoDocumento tipoDocumento;

    @ManyToOne
    @JoinColumn(name = "padrao_caminho_armazenamento_id")
    @JsonBackReference
    private CaminhoPadraoDeArmazenamento padraoArmazenamento;

    @ManyToOne
    @JoinColumn(name = "inativado_por_usuario_id")
    @JsonBackReference
    private Usuario inativadoPorUsuario;

    public Documento() {
    }

    public Documento(Long id, String nomeOriginal, String mimeType, Long tamanhoBytes, String onedriveItemId, String onedriveUrlThumbnail, String onedriveUrlDownload, LocalDateTime dataHoraCriacao, LocalDateTime ultimaAtualizacao, Boolean ativo, LocalDateTime inativadoEm, Usuario criadoPorUsuario, TipoDocumento tipoDocumento, CaminhoPadraoDeArmazenamento padraoArmazenamento, Usuario inativadoPorUsuario) {
        this.id = id;
        this.nomeOriginal = nomeOriginal;
        this.mimeType = mimeType;
        this.tamanhoBytes = tamanhoBytes;
        this.onedriveItemId = onedriveItemId;
        this.onedriveUrlThumbnail = onedriveUrlThumbnail;
        this.onedriveUrlDownload = onedriveUrlDownload;
        this.dataHoraCriacao = dataHoraCriacao;
        this.ultimaAtualizacao = ultimaAtualizacao;
        this.ativo = ativo;
        this.inativadoEm = inativadoEm;
        this.criadoPorUsuario = criadoPorUsuario;
        this.tipoDocumento = tipoDocumento;
        this.padraoArmazenamento = padraoArmazenamento;
        this.inativadoPorUsuario = inativadoPorUsuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }

    public String getOnedriveItemId() {
        return onedriveItemId;
    }

    public void setOnedriveItemId(String onedriveItemId) {
        this.onedriveItemId = onedriveItemId;
    }

    public String getOnedriveUrlThumbnail() {
        return onedriveUrlThumbnail;
    }

    public void setOnedriveUrlThumbnail(String onedriveUrlThumbnail) {
        this.onedriveUrlThumbnail = onedriveUrlThumbnail;
    }

    public LocalDateTime getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public void setDataHoraCriacao(LocalDateTime dataHoraCriacao) {
        this.dataHoraCriacao = dataHoraCriacao;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getInativadoEm() {
        return inativadoEm;
    }

    public void setInativadoEm(LocalDateTime inativadoEm) {
        this.inativadoEm = inativadoEm;
    }

    public Usuario getCriadoPorUsuario() {
        return criadoPorUsuario;
    }

    public void setCriadoPorUsuario(Usuario criadoPorUsuario) {
        this.criadoPorUsuario = criadoPorUsuario;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public CaminhoPadraoDeArmazenamento getPadraoArmazenamento() {
        return padraoArmazenamento;
    }

    public void setPadraoArmazenamento(CaminhoPadraoDeArmazenamento padraoArmazenamento) {
        this.padraoArmazenamento = padraoArmazenamento;
    }

    public Usuario getInativadoPorUsuario() {
        return inativadoPorUsuario;
    }

    public void setInativadoPorUsuario(Usuario inativadoPorUsuario) {
        this.inativadoPorUsuario = inativadoPorUsuario;
    }

    public String getOnedriveUrlDownload() {
        return onedriveUrlDownload;
    }

    public void setOnedriveUrlDownload(String onedriveUrlDownload) {
        this.onedriveUrlDownload = onedriveUrlDownload;
    }
}
