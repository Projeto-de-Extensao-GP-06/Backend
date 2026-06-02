package br.com.sgpj.entity;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.ProcessoDocumento;
import br.com.sgpj.entity.Processo.ProcessoDocumentoId;
import br.com.sgpj.entity.documentos.Documento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("ProcessoDocumento — Testes da entidade")
class ProcessoDocumentoTest {

    @Test
    @DisplayName("Deve criar ProcessoDocumento com construtor parametrizado e retornar campos corretos")
    void deveCriarComConstrutorParametrizado() {
        ProcessoDocumentoId id = new ProcessoDocumentoId(1L, 2L);
        Documento doc = new Documento();
        doc.setId(1L);
        ProcessoApp processo = new ProcessoApp();
        processo.setId(2L);
        Usuario usuario = mock(Usuario.class);
        LocalDateTime agora = LocalDateTime.of(2026, 1, 1, 10, 0);

        ProcessoDocumento pd = new ProcessoDocumento(id, doc, processo, usuario, agora);

        assertAll(
                () -> assertEquals(id, pd.getId()),
                () -> assertEquals(doc, pd.getDocumento()),
                () -> assertEquals(processo, pd.getProcessoApp()),
                () -> assertEquals(usuario, pd.getAnexadoPorUsuario()),
                () -> assertEquals(agora, pd.getAnexadoEm())
        );
    }

    @Test
    @DisplayName("Deve criar ProcessoDocumento com construtor vazio e preencher via setters")
    void deveCriarComConstrutorVazioESetters() {
        ProcessoDocumento pd = new ProcessoDocumento();

        ProcessoDocumentoId id = new ProcessoDocumentoId(5L, 10L);
        Documento doc = new Documento();
        ProcessoApp processo = new ProcessoApp();
        Usuario usuario = mock(Usuario.class);
        LocalDateTime agora = LocalDateTime.of(2026, 6, 1, 9, 30);

        pd.setId(id);
        pd.setDocumento(doc);
        pd.setProcessoApp(processo);
        pd.setAnexadoPorUsuario(usuario);
        pd.setAnexadoEm(agora);

        assertAll(
                () -> assertEquals(id, pd.getId()),
                () -> assertEquals(doc, pd.getDocumento()),
                () -> assertEquals(processo, pd.getProcessoApp()),
                () -> assertEquals(usuario, pd.getAnexadoPorUsuario()),
                () -> assertEquals(agora, pd.getAnexadoEm())
        );
    }

    @Test
    @DisplayName("ProcessoDocumentoId deve armazenar documentoId e processoAppId corretamente")
    void processoDocumentoIdDeveArmazenarIds() {
        ProcessoDocumentoId id = new ProcessoDocumentoId(3L, 7L);

        assertEquals(3L, id.documentoId());
        assertEquals(7L, id.processoAppId());
    }

    @Test
    @DisplayName("ProcessoDocumentoId — dois registros iguais devem ser iguais (record equals)")
    void processoDocumentoIdEquality() {
        ProcessoDocumentoId id1 = new ProcessoDocumentoId(3L, 7L);
        ProcessoDocumentoId id2 = new ProcessoDocumentoId(3L, 7L);
        ProcessoDocumentoId idDiferente = new ProcessoDocumentoId(3L, 99L);

        assertEquals(id1, id2);
        assertNotEquals(id1, idDiferente);
    }
}

