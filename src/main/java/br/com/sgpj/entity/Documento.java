package br.com.sgpj.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "criado_por_usuario_id", nullable = false)
    private Usuario criadoPorUsuario;

    @ManyToOne
    @JoinColumn(name = "tipo_documento_id", nullable = false)
    private TipoDocumento tipoDocumento;

    @ManyToOne
    @JoinColumn(name = "padrao_caminho_armazenamento_id")
    private PadraoCaminhoArmazenamento padraoCaminhoArmazenamento;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "tamanho_bytes", nullable = false)
    private Long tamanhoBytes;

    @Column(name = "onedrive_item_id", length = 255)
    private String onedriveItemId;

    @Column(name = "onedrive_url_download", columnDefinition = "TEXT")
    private String onedriveUrlDownload;

    @Column(name = "onedrive_url_thumbnail", columnDefinition = "TEXT")
    private String onedriveUrlThumbnail;

    @Column(name = "data_hora_criacao", nullable = false)
    private LocalDateTime dataHoraCriacao;

    @Column(name = "ultima_atualizacao", nullable = false)
    private LocalDateTime ultimaAtualizacao;

    @Column(nullable = false)
    private Boolean ativo;

    @Column(name = "inativado_em")
    private LocalDateTime inativadoEm;

    @ManyToOne
    @JoinColumn(name = "inativado_por_usuario_id")
    private Usuario inativadoPorUsuario;

    public Documento() {}

    @PrePersist
    public void prePersist() {
        this.dataHoraCriacao = LocalDateTime.now();
        this.ultimaAtualizacao = LocalDateTime.now();
        this.ativo = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.ultimaAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public PadraoCaminhoArmazenamento getPadraoCaminhoArmazenamento() {
        return padraoCaminhoArmazenamento;
    }

    public void setPadraoCaminhoArmazenamento(PadraoCaminhoArmazenamento padraoCaminhoArmazenamento) {
        this.padraoCaminhoArmazenamento = padraoCaminhoArmazenamento;
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

    public String getOnedriveUrlDownload() {
        return onedriveUrlDownload;
    }

    public void setOnedriveUrlDownload(String onedriveUrlDownload) {
        this.onedriveUrlDownload = onedriveUrlDownload;
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

    public Usuario getInativadoPorUsuario() {
        return inativadoPorUsuario;
    }

    public void setInativadoPorUsuario(Usuario inativadoPorUsuario) {
        this.inativadoPorUsuario = inativadoPorUsuario;
    }
}

