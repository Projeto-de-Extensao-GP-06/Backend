package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.OneDriveItemResponse;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.ProcessoDocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import br.com.sgpj.service.RegistroHistoricoProcesso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OneDriveDocumentManager")
class OneDriveDocumentManagerTest {

    @Mock private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock private CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    @Mock private ProcessoAppRepository processoAppRepository;
    @Mock private ProcessoDocumentoRepository processoDocumentoRepository;
    @Mock private RegistroHistoricoProcesso registroHistorico;

    private OneDriveDocumentManager manager;

    @BeforeEach
    void setUp() {
        manager = new OneDriveDocumentManager(
                usuarioAutenticacaoAdapter,
                usuarioRepository,
                documentoRepository,
                tipoDocumentoRepository,
                caminhoRepository,
                processoAppRepository,
                processoDocumentoRepository,
                registroHistorico
        );
    }

    @Test
    @DisplayName("Deve registrar histórico ao vincular documento a um processo")
    void deveRegistrarHistoricoAoVincularDocumentoAoProcesso() {
        Usuario usuario = new Usuario(2L, "Usuário", "usuario@sgpj.com", null, null, null, true);
        TipoDocumento tipoDocumento = new TipoDocumento(7L, "Petição", null);
        ProcessoApp processo = new ProcessoApp();
        processo.setId(3L);
        CaminhoPadraoDeArmazenamento caminho = new CaminhoPadraoDeArmazenamento();
        OneDriveItemResponse item = new OneDriveItemResponse(
                "drive-item-1", "peticao.pdf", "arquivo", 1024L, "application/pdf",
                "https://download", "https://preview", null, null, null
        );

        when(usuarioAutenticacaoAdapter.obterEmailUsuarioLogado()).thenReturn("usuario@sgpj.com");
        when(usuarioRepository.findByEmail("usuario@sgpj.com")).thenReturn(usuario);
        when(tipoDocumentoRepository.findById(7L)).thenReturn(Optional.of(tipoDocumento));
        when(documentoRepository.findByOnedriveItemId("drive-item-1")).thenReturn(Optional.empty());
        when(documentoRepository.save(any(Documento.class))).thenAnswer(invocation -> {
            Documento documento = invocation.getArgument(0);
            documento.setId(9L);
            return documento;
        });
        when(processoAppRepository.findById(3L)).thenReturn(Optional.of(processo));

        Documento documentoSalvo = manager.registrarDocumento(item, 7L, caminho, 3L);

        ArgumentCaptor<Documento> documentoCaptor = ArgumentCaptor.forClass(Documento.class);
        verify(documentoRepository).save(documentoCaptor.capture());
        verify(processoDocumentoRepository).save(any());
        verify(registroHistorico).registrar(
                eq(processo),
                eq(usuario),
                eq(RegistroHistoricoProcesso.DOCUMENTO),
                contains("peticao.pdf")
        );
        assertEquals(9L, documentoSalvo.getId());
        assertEquals(tipoDocumento, documentoCaptor.getValue().getTipoDocumento());
        assertNotNull(documentoCaptor.getValue().getDataHoraCriacao());
    }
}
