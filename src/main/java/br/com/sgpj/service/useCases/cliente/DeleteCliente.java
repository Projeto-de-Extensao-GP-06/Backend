package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Endereco;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.exception.ClienteNaoEncontradoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.EnderecoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.service.useCases.processo.DeleteHistoricoProcesso;
import br.com.sgpj.service.useCases.processo.DeleteProcessoApp;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteCliente {

    private final ClienteRepository clienteRepository;
    private final EnderecoRepository enderecoRepository;
    private final ProcessoAppRepository processoAppRepository;
    private final DeletarDocumentosCliente deletarDocumentosCliente;
    private final DeletarContatosCliente deletarContatosCliente;
    private final DeleteProcessoApp deleteProcessoApp;
    private final DeleteHistoricoProcesso deleteHistoricoProcesso;

    public DeleteCliente(ClienteRepository clienteRepository,
                         EnderecoRepository enderecoRepository,
                         ProcessoAppRepository processoAppRepository,
                         DeletarDocumentosCliente deletarDocumentosCliente,
                         DeletarContatosCliente deletarContatosCliente,
                         DeleteProcessoApp deleteProcessoApp,
                         DeleteHistoricoProcesso deleteHistoricoProcesso) {
        this.clienteRepository = clienteRepository;
        this.enderecoRepository = enderecoRepository;
        this.processoAppRepository = processoAppRepository;
        this.deletarDocumentosCliente = deletarDocumentosCliente;
        this.deletarContatosCliente = deletarContatosCliente;
        this.deleteProcessoApp = deleteProcessoApp;
        this.deleteHistoricoProcesso = deleteHistoricoProcesso;
    }

    public void deletarCliente(Long id) {
        Cliente cliente = clienteRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(id));
        Endereco endereco = cliente.getEndereco();

        // Remove dependências dos processos do cliente
        List<ProcessoApp> processos = processoAppRepository.findByClienteId(id);
        for (ProcessoApp processo : processos) {
            deleteHistoricoProcesso.deletarPorProcessoApp(processo);
        }
        deleteProcessoApp.deletarProcessos(processos);

        // Remove documentos e caminhos do cliente
        deletarDocumentosCliente.deletarDocumentosCliente(id);

        // Remove contatos
        deletarContatosCliente.deletarContatos(cliente);

        // Remove o cliente
        clienteRepository.delete(cliente);

        if (endereco != null) {
            enderecoRepository.delete(endereco);
        }
    }
}
