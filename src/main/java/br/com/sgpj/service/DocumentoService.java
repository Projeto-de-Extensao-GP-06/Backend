package br.com.sgpj.service;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.response.CaminhoArmazenamentoResponse;
import br.com.sgpj.dto.response.CaminhoClienteResponse;
import br.com.sgpj.dto.response.DocumentoResponse;
import br.com.sgpj.dto.response.TipoDocumentoResponse;
import br.com.sgpj.entity.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Documento;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.mapper.DocumentoMapper;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.DocumentoRepository;
import br.com.sgpj.repository.TipoDocumentoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentoService {

    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final TipoDocumentoRepository tipoDocumentoRepository;
    private final CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    private final DocumentoRepository documentoRepository;
    private final ClienteRepository clienteRepository;

    public DocumentoService(UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
                            TipoDocumentoRepository tipoDocumentoRepository,
                            CaminhoPadraoDeArmazenamentoRepository caminhoRepository,
                            DocumentoRepository documentoRepository,
                            ClienteRepository clienteRepository) {
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
        this.tipoDocumentoRepository = tipoDocumentoRepository;
        this.caminhoRepository = caminhoRepository;
        this.documentoRepository = documentoRepository;
        this.clienteRepository = clienteRepository;
    }

    public boolean isCorrectScope(HttpServletRequest httpRequest, String scope) {
        return usuarioAutenticacaoAdapter.isCorrectScope(httpRequest, scope);
    }

    public List<TipoDocumentoResponse> listarTiposDocumento() {
        return DocumentoMapper.toTipoDocumentoResponseList(tipoDocumentoRepository.findAll());
    }

    public List<CaminhoArmazenamentoResponse> listarCaminhosArmazenamento() {
        return DocumentoMapper.toCaminhoResponseList(caminhoRepository.findAll());
    }

    public List<DocumentoResponse> listarDocumentosPorCaminho(Long caminhoDeArmazenamentoId) {
        List<Documento> documentos = documentoRepository.findByPadraoArmazenamentoIdAndAtivoTrue(caminhoDeArmazenamentoId);
        return DocumentoMapper.toDocumentoResponseList(documentos);
    }



    /**
     * Retorna todos os caminhos de armazenamento do cliente formatados para o front.
     * Exemplo: [{ id: 3, descricao: "Rafael Silva - 12345678901", caminho: "rafael-silva-12345678901/1234567-89.2024" }]
     * O front usa o 'id' retornado para passar ao OneDriveController.
     */
    public List<CaminhoClienteResponse> listarCaminhosDoCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));

        List<CaminhoPadraoDeArmazenamento> caminhos = caminhoRepository.findByClienteId(clienteId);

        if (caminhos.isEmpty()) {
            return List.of();
        }

        String prefixo = cliente.getNomeRazaoSocial() + " - " +
                (cliente.getCpfCnpj() != null ? cliente.getCpfCnpj() : "sem documento");

        return caminhos.stream()
                .map(c -> new CaminhoClienteResponse(c.getId(), prefixo, c.getCaminho()))
                .toList();
    }
}
