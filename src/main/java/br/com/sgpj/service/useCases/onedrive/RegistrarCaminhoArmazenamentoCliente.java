package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.documentos.CaminhoPadraoDeArmazenamento;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RegistrarCaminhoArmazenamentoCliente implements ClienteCaminhoArmazenamento {

    private final CaminhoPadraoDeArmazenamentoRepository caminhoRepository;

    public RegistrarCaminhoArmazenamentoCliente(CaminhoPadraoDeArmazenamentoRepository caminhoRepository) {
        this.caminhoRepository = caminhoRepository;
    }

    @Override
    @Transactional
    public void registrar(Cliente cliente) {
        String nomeBase = OneDrivePathHelper.gerarNomePasta(cliente);
        if (!caminhoRepository.existsByCaminho(nomeBase)) {
            CaminhoPadraoDeArmazenamento novoCaminho = new CaminhoPadraoDeArmazenamento();
            novoCaminho.setCliente(cliente);
            novoCaminho.setCaminho(nomeBase);
            caminhoRepository.save(novoCaminho);
        }
    }
}
