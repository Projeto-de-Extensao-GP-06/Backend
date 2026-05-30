package br.com.sgpj.service;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.CaminhoClienteResponse;
import br.com.sgpj.dto.response.DocumentoResponse;
import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.documentos.Documento;
import br.com.sgpj.entity.documentos.TipoDocumento;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentoService — Testes unitários")
class DocumentoServiceTest {

    @Mock private UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    @Mock private TipoDocumentoRepository tipoDocumentoRepository;
    @Mock private CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    @Mock private DocumentoRepository documentoRepository;
    @Mock private ClienteRepository clienteRepository;

    private DocumentoService service;

    @BeforeEach
    void setup() {
        service = new DocumentoService(
                usuarioAutenticacaoAdapter,
                tipoDocumentoRepository,
                caminhoRepository,
                documentoRepository,
                clienteRepository
        );
    }

    // ========== listarTiposDocumento ==========

    @Nested
    @DisplayName("listarTiposDocumento")
    class ListarTiposDocumentoTest {

        @Test
        @DisplayName("Deve retornar lista de tipos")
        void deveRetornarListaDeTipos() {
            TipoDocumento t1 = new TipoDocumento(); t1.setId(1L); t1.setNome("RG");
            TipoDocumento t2 = new TipoDocumento(); t2.setId(2L); t2.setNome("CTPS");
            when(tipoDocumentoRepository.findAll()).thenReturn(List.of(t1, t2));

            List<TipoDocumentoResponse> result = service.listarTiposDocumento();

            assertEquals(2, result.size());
            assertEquals("RG", result.get(0).nome());
            assertEquals("CTPS", result.get(1).nome());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há tipos")
        void deveRetornarVazio() {
            when(tipoDocumentoRepository.findAll()).thenReturn(Collections.emptyList());
            assertTrue(service.listarTiposDocumento().isEmpty());
        }
    }


    // ========== listarDocumentosPorCaminho ==========

    @Nested
    @DisplayName("listarDocumentosPorCaminho")
    class ListarDocumentosPorCaminhoTest {

        @Test
        @DisplayName("Deve retornar documentos de um caminho")
        void deveRetornarDocumentos() {
            Documento doc = criarDocumentoMock(1L, "contrato.pdf");
            when(documentoRepository.findByPadraoArmazenamentoIdAndAtivoTrue(5L)).thenReturn(List.of(doc));

            List<DocumentoResponse> result = service.listarDocumentosPorCaminho(5L);

            assertEquals(1, result.size());
            assertEquals("contrato.pdf", result.get(0).nomeOriginal());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando sem documentos")
        void deveRetornarVazio() {
            when(documentoRepository.findByPadraoArmazenamentoIdAndAtivoTrue(99L)).thenReturn(Collections.emptyList());
            assertTrue(service.listarDocumentosPorCaminho(99L).isEmpty());
        }
    }


    // ========== listarCaminhosDoCliente ==========

    @Nested
    @DisplayName("listarCaminhosDoCliente")
    class ListarCaminhosDoClienteTest {

        @Test
        @DisplayName("Deve retornar caminhos formatados do cliente")
        void deveRetornarCaminhosDoCliente() {
            Cliente cliente = new Cliente();
            cliente.setId(1L);
            cliente.setNomeRazaoSocial("Rafael Silva");
            cliente.setCpfCnpj("12345678901");
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

            CaminhoPadraoDeArmazenamento c1 = new CaminhoPadraoDeArmazenamento();
            c1.setId(10L); c1.setCaminho("rafael-silva-12345678901/1234567");
            CaminhoPadraoDeArmazenamento c2 = new CaminhoPadraoDeArmazenamento();
            c2.setId(11L); c2.setCaminho("rafael-silva-12345678901/9876543");
            when(caminhoRepository.findByClienteId(1L)).thenReturn(List.of(c1, c2));

            List<CaminhoClienteResponse> result = service.listarCaminhosDoCliente(1L);

            assertEquals(2, result.size());
            assertEquals(10L, result.get(0).id());
            assertEquals("Rafael Silva - 12345678901", result.get(0).descricao());
            assertEquals("rafael-silva-12345678901/1234567", result.get(0).caminho());
            assertEquals(11L, result.get(1).id());
        }

        @Test
        @DisplayName("Deve retornar 'sem documento' quando cpfCnpj é nulo")
        void deveRetornarSemDocumentoQuandoCpfNulo() {
            Cliente cliente = new Cliente();
            cliente.setId(2L);
            cliente.setNomeRazaoSocial("Empresa XYZ");
            cliente.setCpfCnpj(null);
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));

            CaminhoPadraoDeArmazenamento c = new CaminhoPadraoDeArmazenamento();
            c.setId(20L); c.setCaminho("empresa-xyz/111");
            when(caminhoRepository.findByClienteId(2L)).thenReturn(List.of(c));

            List<CaminhoClienteResponse> result = service.listarCaminhosDoCliente(2L);

            assertEquals(1, result.size());
            assertTrue(result.get(0).descricao().contains("sem documento"));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando cliente sem caminhos")
        void deveRetornarVazioSemCaminhos() {
            Cliente cliente = new Cliente();
            cliente.setId(3L);
            cliente.setNomeRazaoSocial("Teste");
            when(clienteRepository.findById(3L)).thenReturn(Optional.of(cliente));
            when(caminhoRepository.findByClienteId(3L)).thenReturn(Collections.emptyList());

            List<CaminhoClienteResponse> result = service.listarCaminhosDoCliente(3L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não existe")
        void deveLancarExcecaoClienteNaoExiste() {
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ClienteNaoEncontradoException.class, () -> service.listarCaminhosDoCliente(99L));
        }
    }

    private Documento criarDocumentoMock(Long id, String nome) {
        Documento doc = new Documento();
        doc.setId(id);
        doc.setNomeOriginal(nome);
        doc.setMimeType("application/pdf");
        doc.setTamanhoBytes(1024L);
        doc.setOnedriveItemId("item-" + id);
        doc.setAtivo(true);
        doc.setDataHoraCriacao(LocalDateTime.now());
        doc.setUltimaAtualizacao(LocalDateTime.now());
        return doc;
    }
}

