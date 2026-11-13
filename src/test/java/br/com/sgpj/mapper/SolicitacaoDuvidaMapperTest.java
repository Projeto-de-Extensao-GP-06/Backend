package br.com.sgpj.mapper;

import br.com.sgpj.dto.response.Solicitacao.SolicitacaoClientePreenchimentoResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaDetalhesResponse;
import br.com.sgpj.dto.response.Solicitacao.SolicitacaoDuvidaListarResponse;
import br.com.sgpj.entity.ComoConheceuEnum;
import br.com.sgpj.entity.SolicitacaoDuvida;
import br.com.sgpj.entity.SolicitacaoStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("SolicitacaoDuvidaMapper")
class SolicitacaoDuvidaMapperTest {

    @Test
    @DisplayName("Deve mapear corretamente para SolicitacaoClientePreenchimentoResponse quando PJ possui nomeContato e cnpj")
    void deveMapearParaPreenchimentoPjComNomeContatoECnpj() {
        SolicitacaoDuvida solicitacao = new SolicitacaoDuvida();
        solicitacao.setId(1L);
        solicitacao.setClienteId(10L);
        solicitacao.setNomeCompleto("Empresa Beta LTDA");
        solicitacao.setNomeContato("Rodrigo Silva");
        solicitacao.setCnpj("11222333000181");
        solicitacao.setEmail("rodrigo@beta.com");
        solicitacao.setTelefone("11988887777");
        solicitacao.setEstado("SP");
        solicitacao.setNumeroProcesso("00000010020268260001");
        solicitacao.setDescricao("Revisão contratual");
        solicitacao.setComoConheceu(ComoConheceuEnum.INSTAGRAM);

        SolicitacaoClientePreenchimentoResponse response = SolicitacaoDuvidaMapper.toClientePreenchimentoResponse(solicitacao);

        assertEquals(1L, response.solicitacaoId());
        assertEquals(10L, response.clienteId());
        assertEquals("Empresa Beta LTDA", response.nomeRazaoSocial());
        assertEquals("Rodrigo Silva", response.nomeContato());
        assertEquals("11222333000181", response.cnpj());
        assertEquals("rodrigo@beta.com", response.email());
        assertEquals("11988887777", response.telefone());
        assertEquals("SP", response.estado());
        assertEquals("00000010020268260001", response.numeroProcesso());
        assertEquals("Revisão contratual", response.descricao());
        assertEquals(ComoConheceuEnum.INSTAGRAM, response.comoConheceu());
        assertNull(response.processo());
    }

    @Test
    @DisplayName("Deve fazer fallback de nomeContato para nomeCompleto quando nomeContato for nulo")
    void deveFazerFallbackNomeContatoQuandoNulo() {
        SolicitacaoDuvida solicitacao = new SolicitacaoDuvida();
        solicitacao.setId(2L);
        solicitacao.setClienteId(20L);
        solicitacao.setNomeCompleto("Ana Paula Oliveira");
        solicitacao.setNomeContato(null);
        solicitacao.setCnpj(null);
        solicitacao.setEmail("ana@email.com");

        SolicitacaoClientePreenchimentoResponse response = SolicitacaoDuvidaMapper.toClientePreenchimentoResponse(solicitacao);

        assertEquals("Ana Paula Oliveira", response.nomeRazaoSocial());
        assertEquals("Ana Paula Oliveira", response.nomeContato());
        assertNull(response.cnpj());
    }

    @Test
    @DisplayName("Deve mapear corretamente para toListarResponse")
    void deveMapearParaListarResponse() {
        LocalDateTime agora = LocalDateTime.now();
        SolicitacaoDuvida solicitacao = new SolicitacaoDuvida();
        solicitacao.setId(3L);
        solicitacao.setClienteId(30L);
        solicitacao.setNomeCompleto("Lucas Santos");
        solicitacao.setEmail("lucas@email.com");
        solicitacao.setTelefone("11999990000");
        solicitacao.setEstado("RJ");
        solicitacao.setNumeroProcesso("00000020020268260001");
        solicitacao.setDescricao("Dúvida trabalhista");
        solicitacao.setProfissao("Analista");
        solicitacao.setComoConheceu(ComoConheceuEnum.INDICACAO);
        solicitacao.setDataSolicitacao(agora);
        solicitacao.setUltimaAtualizacao(agora);
        solicitacao.setStatus(SolicitacaoStatus.PENDENTE);

        SolicitacaoDuvidaListarResponse response = SolicitacaoDuvidaMapper.toListarResponse(solicitacao);

        assertEquals(3L, response.id());
        assertEquals(30L, response.clienteId());
        assertEquals("Lucas Santos", response.nomeCompleto());
        assertEquals("Analista", response.profissao());
        assertEquals("lucas@email.com", response.email());
        assertEquals(SolicitacaoStatus.PENDENTE, response.status());
        assertEquals(agora, response.ultimaAtualizacao());
    }

    @Test
    @DisplayName("Deve mapear corretamente para toDetalhesResponse")
    void deveMapearParaDetalhesResponse() {
        LocalDateTime agora = LocalDateTime.now();
        SolicitacaoDuvida solicitacao = new SolicitacaoDuvida();
        solicitacao.setId(4L);
        solicitacao.setClienteId(40L);
        solicitacao.setNomeCompleto("Julia Mendes");
        solicitacao.setEmail("julia@email.com");
        solicitacao.setTelefone("11977778888");
        solicitacao.setEstado("MG");
        solicitacao.setNumeroProcesso(null);
        solicitacao.setDescricao("Caso cível");
        solicitacao.setProfissao("Advogada");
        solicitacao.setComoConheceu(ComoConheceuEnum.OUTROS);
        solicitacao.setAceitouTermos(true);
        solicitacao.setDataSolicitacao(agora);
        solicitacao.setUltimaAtualizacao(agora);
        solicitacao.setStatus(SolicitacaoStatus.ACEITA);
        solicitacao.setDataResposta(agora.plusDays(1));

        SolicitacaoDuvidaDetalhesResponse response = SolicitacaoDuvidaMapper.toDetalhesResponse(solicitacao);

        assertEquals(4L, response.id());
        assertEquals("Julia Mendes", response.nomeCompleto());
        assertEquals("Advogada", response.profissao());
        assertTrue(response.aceitouTermos());
        assertEquals(SolicitacaoStatus.ACEITA, response.status());
        assertEquals(agora, response.ultimaAtualizacao());
    }
}
