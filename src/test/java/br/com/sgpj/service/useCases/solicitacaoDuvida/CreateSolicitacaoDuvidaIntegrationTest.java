package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.dto.request.SolicitacaoDuvidaCriacaoRequest;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.StatusCliente;
import br.com.sgpj.repository.CaminhoPadraoDeArmazenamentoRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.StatusClienteRepository;
import br.com.sgpj.service.useCases.onedrive.RegistrarCaminhoArmazenamentoCliente;
import br.com.sgpj.producer.SincronizacaoProcessoProducer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@Import({CreateSolicitacaoDuvida.class, RegistrarCaminhoArmazenamentoCliente.class,
        PrepararVinculoProcessoOficial.class})
class CreateSolicitacaoDuvidaIntegrationTest {

    @Autowired private CreateSolicitacaoDuvida createSolicitacaoDuvida;
    @Autowired private CaminhoPadraoDeArmazenamentoRepository caminhoRepository;
    @Autowired private ProcessoAppRepository processoAppRepository;
    @Autowired private StatusClienteRepository statusClienteRepository;
    @Autowired private EntityManager entityManager;
    @MockitoBean private SincronizacaoProcessoProducer sincronizacaoProducer;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void devePersistirCaminhoDaSolicitacaoSemIntegracaoOneDrive(boolean pessoaFisica) {
        StatusCliente status = new StatusCliente();
        status.setNome("LEAD");
        statusClienteRepository.save(status);
        SolicitacaoDuvidaCriacaoRequest request = new SolicitacaoDuvidaCriacaoRequest(
                "Cliente de Teste", "cliente@example.com", pessoaFisica, "11999999999",
                null, "Descricao do caso", ComoConheceuEnum.SITE, true,
                pessoaFisica ? null : "Empresa de Teste", pessoaFisica ? null : "12345678000195");

        createSolicitacaoDuvida.criarSolicitacao(request);
        entityManager.flush();
        entityManager.clear();

        var processo = processoAppRepository.findAll().getFirst();
        var cliente = processo.getCliente();
        var caminhos = caminhoRepository.findByClienteId(cliente.getId());
        assertEquals(StatusProcesso.SOLICITACAO, processo.getStatus());
        assertEquals("LEAD", cliente.getStatusCliente().getNome());
        assertEquals(1, caminhos.size());
        assertEquals(pessoaFisica ? "cliente-de-teste" : "empresa-de-teste", caminhos.getFirst().getCaminho());
        assertEquals(cliente.getId(), caminhos.getFirst().getCliente().getId());
        verifyNoInteractions(sincronizacaoProducer);
    }
}
