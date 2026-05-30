package br.com.sgpj.entity.Processo;

import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.Documento;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processo_documento")
public class ProcessoDocumento {

    @EmbeddedId
    private ProcessoDocumentoId id;

    @ManyToOne
    @MapsId("documentoId")
    @JoinColumn(name = "documento_id")
    private Documento documento;

    @ManyToOne
    @MapsId("processoAppId")
    @JoinColumn(name = "processo_dados_aplicacao_id")
    private ProcessoApp processoApp;

    @ManyToOne
    @JoinColumn(name = "anexado_por_usuario_id")
    private Usuario anexadoPorUsuario;

    @Column(name = "anexado_em")
    private LocalDateTime anexadoEm;

    public ProcessoDocumento() {
    }

    public ProcessoDocumento(ProcessoDocumentoId id, Documento documento, ProcessoApp processoApp, Usuario anexadoPorUsuario, LocalDateTime anexadoEm) {
        this.id = id;
        this.documento = documento;
        this.processoApp = processoApp;
        this.anexadoPorUsuario = anexadoPorUsuario;
        this.anexadoEm = anexadoEm;
    }

    public ProcessoDocumentoId getId() {
        return id;
    }

    public void setId(ProcessoDocumentoId id) {
        this.id = id;
    }

    public Documento getDocumento() {
        return documento;
    }

    public void setDocumento(Documento documento) {
        this.documento = documento;
    }

    public ProcessoApp getProcessoApp() {
        return processoApp;
    }

    public void setProcessoApp(ProcessoApp processoApp) {
        this.processoApp = processoApp;
    }

    public Usuario getAnexadoPorUsuario() {
        return anexadoPorUsuario;
    }

    public void setAnexadoPorUsuario(Usuario anexadoPorUsuario) {
        this.anexadoPorUsuario = anexadoPorUsuario;
    }

    public LocalDateTime getAnexadoEm() {
        return anexadoEm;
    }

    public void setAnexadoEm(LocalDateTime anexadoEm) {
        this.anexadoEm = anexadoEm;
    }
}
