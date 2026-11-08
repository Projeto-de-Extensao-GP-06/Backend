package br.com.sgpj.mapper;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.Contato;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.SolicitacaoDuvida;
import br.com.sgpj.entity.SolicitacaoStatus;
import br.com.sgpj.entity.StatusCliente;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ProcessoAppToSolicitacaoDuvidaMapper")
class ProcessoAppToSolicitacaoDuvidaMapperTest {

    @Test
    @DisplayName("Deve mapear corretamente ProcessoApp com dados de PJ, contato principal e CNPJ informado")
    void deveMapearProcessoAppComDadosDePjEContatoPrincipal() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNomeRazaoSocial("Tecnologia LTDA");
        cliente.setPF(false);
        cliente.setComoConheceu(ComoConheceuEnum.SITE);

        StatusCliente status = new StatusCliente();
        status.setNome("ATIVO");
        cliente.setStatusCliente(status);

        LocalDateTime transformacao = LocalDateTime.of(2026, 10, 1, 10, 0);
        cliente.setDataHoraTransformacaoEmCliente(transformacao);

        Contato contatoSecundario = new Contato();
        contatoSecundario.setNome("Secundario");
        contatoSecundario.setEmail("secundario@empresa.com");
        contatoSecundario.setTelefone("11111111111");
        contatoSecundario.setPrincipal(false);

        Contato contatoPrincipal = new Contato();
        contatoPrincipal.setNome("Carlos Gerente");
        contatoPrincipal.setEmail("carlos@empresa.com");
        contatoPrincipal.setTelefone("11999998888");
        contatoPrincipal.setPrincipal(true);

        cliente.setContatos(List.of(contatoSecundario, contatoPrincipal));

        ProcessoApi processoApi = new ProcessoApi();
        processoApi.setNumeroCnj("00000010020268260001");

        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setId(1L);
        processoApp.setCliente(cliente);
        processoApp.setDescricaoCaso("Dúvida sobre contrato");
        processoApp.setObservacao("Solicitação enviada pelo formulário público. Estado: SP");
        LocalDateTime inicio = LocalDateTime.of(2026, 9, 20, 15, 30);
        processoApp.setDataHoraInicio(inicio);
        processoApp.setUltimaAtualizacao(inicio);
        processoApp.setCnpjInformadoFormulario("12345678000195");
        processoApp.setProcessoApi(processoApi);

        SolicitacaoDuvida solicitacao = ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(processoApp);

        assertEquals(1L, solicitacao.getId());
        assertEquals(10L, solicitacao.getClienteId());
        assertEquals("Tecnologia LTDA", solicitacao.getNomeCompleto());
        assertEquals("Carlos Gerente", solicitacao.getNomeContato());
        assertEquals(false, solicitacao.getPF());
        var detalhes = SolicitacaoDuvidaMapper.toDetalhesResponse(solicitacao);
        var listagem = SolicitacaoDuvidaMapper.toListarResponse(solicitacao);
        assertEquals(false, detalhes.isPF());
        assertEquals("Tecnologia LTDA", detalhes.nomeRazaoSocial());
        assertEquals("Carlos Gerente", detalhes.nomeContato());
        assertEquals("12345678000195", detalhes.cnpj());
        assertEquals(inicio, detalhes.ultimaAtualizacao());
        assertEquals(false, listagem.isPF());
        assertEquals("Tecnologia LTDA", listagem.nomeRazaoSocial());
        assertEquals("Carlos Gerente", listagem.nomeContato());
        assertEquals("12345678000195", listagem.cnpj());
        assertEquals(inicio, listagem.ultimaAtualizacao());
        assertEquals(inicio, solicitacao.getUltimaAtualizacao());
        assertEquals("12345678000195", solicitacao.getCnpj());
        assertEquals("carlos@empresa.com", solicitacao.getEmail());
        assertEquals("11999998888", solicitacao.getTelefone());
        assertEquals("SP", solicitacao.getEstado());
        assertEquals("00000010020268260001", solicitacao.getNumeroProcesso());
        assertEquals("Dúvida sobre contrato", solicitacao.getDescricao());
        assertEquals(ComoConheceuEnum.SITE, solicitacao.getComoConheceu());
        assertTrue(solicitacao.getAceitouTermos());
        assertEquals(SolicitacaoStatus.ACEITA, solicitacao.getStatus());
        assertEquals(transformacao, solicitacao.getDataResposta());
        assertEquals(processoApi, solicitacao.getProcessoApi());
    }

    @Test
    @DisplayName("Deve mapear corretamente quando cliente não tem contatos e status é INATIVO")
    void deveMapearQuandoClienteSemContatosEStatusInativo() {
        Cliente cliente = new Cliente();
        cliente.setId(15L);
        cliente.setNomeRazaoSocial("Fulano da Silva");
        cliente.setPF(true);

        StatusCliente status = new StatusCliente();
        status.setNome("INATIVO");
        cliente.setStatusCliente(status);
        cliente.setContatos(Collections.emptyList());

        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setId(2L);
        processoApp.setCliente(cliente);
        processoApp.setObservacao("Texto qualquer sem prefixo padrão");

        SolicitacaoDuvida solicitacao = ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(processoApp);

        assertEquals(15L, solicitacao.getClienteId());
        assertEquals("Fulano da Silva", solicitacao.getNomeCompleto());
        assertEquals(true, solicitacao.getPF());
        var detalhes = SolicitacaoDuvidaMapper.toDetalhesResponse(solicitacao);
        var listagem = SolicitacaoDuvidaMapper.toListarResponse(solicitacao);
        assertEquals(true, detalhes.isPF());
        assertNull(detalhes.nomeRazaoSocial());
        assertEquals(true, listagem.isPF());
        assertNull(listagem.nomeRazaoSocial());
        assertNull(solicitacao.getNomeContato());
        assertNull(solicitacao.getCnpj());
        assertNull(solicitacao.getEmail());
        assertNull(solicitacao.getTelefone());
        assertNull(solicitacao.getEstado());
        assertEquals(SolicitacaoStatus.NEGADA, solicitacao.getStatus());
    }

    @Test
    @DisplayName("Deve definir status PENDENTE quando status do cliente for LEAD")
    void deveDefinirStatusPendenteParaLead() {
        Cliente cliente = new Cliente();
        cliente.setId(20L);
        StatusCliente status = new StatusCliente();
        status.setNome("LEAD");
        cliente.setStatusCliente(status);

        ProcessoApp processoApp = new ProcessoApp();
        processoApp.setCliente(cliente);

        SolicitacaoDuvida solicitacao = ProcessoAppToSolicitacaoDuvidaMapper.toSolicitacaoDuvida(processoApp);

        assertEquals(SolicitacaoStatus.PENDENTE, solicitacao.getStatus());
    }
}
