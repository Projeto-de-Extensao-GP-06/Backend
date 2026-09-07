package br.com.sgpj.service.useCases.cliente;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.repository.ContatoRepository;
import org.springframework.stereotype.Component;

@Component
public class DeletarContatosCliente {

    private final ContatoRepository contatoRepository;

    public DeletarContatosCliente(ContatoRepository contatoRepository) {
        this.contatoRepository = contatoRepository;
    }

    public void deletarContatos(Cliente cliente) {
        if (cliente.getContatos() != null && !cliente.getContatos().isEmpty()) {
            contatoRepository.deleteAll(cliente.getContatos());
        }
    }
}
