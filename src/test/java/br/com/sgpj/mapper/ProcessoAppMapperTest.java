package br.com.sgpj.mapper;

import br.com.sgpj.dto.request.ProcessoAppCriacaoRequest;
import br.com.sgpj.dto.request.ProcessoAppUpdateRequest;
import br.com.sgpj.dto.response.Processos.HistoricoProcessoResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppCreateResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppMiniResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppResponse;
import br.com.sgpj.dto.response.Processos.ProcessoAppUpdateResponse;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.FaseProcesso;
import br.com.sgpj.entity.Processo.MotivoStatusProcesso;
import br.com.sgpj.entity.Processo.Prioridade;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Processo.SiglaTribunal;
import br.com.sgpj.entity.Processo.StatusProcesso;
import br.com.sgpj.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("ProcessoAppMapper")
class ProcessoAppMapperTest {

    @Nested
    @DisplayName("toCreateEntity")
    class ToCreateEntityTest {

        @Test
        @DisplayName("Deve mapear ProcessoAppCriacaoRequest para entidade com status ATIVO inicial")
        void deveMapearCriacaoRequestParaEntidadeComStatusAtivo() {
            LocalDateTime dataAudiencia = LocalDateTime.of(2026, 12, 1, 14, 0);
            ProcessoAppCriacaoRequest request = new ProcessoAppCriacaoRequest(
                    1L,
                    "Descrição do caso",
                    "Observação",
                    2L,
                    3L,
                    dataAudiencia,
                    "0000000-00.0000.0.00.0000",
                    5000.0
            );

            ProcessoApp entidade = ProcessoAppMapper.toCreateEntity(request);

            assertNotNull(entidade);
            assertEquals("Descrição do caso", entidade.getDescricaoCaso());
            assertEquals("Observação", entidade.getObservacao());
            assertEquals(StatusProcesso.ATIVO, entidade.getStatus());
            assertEquals(dataAudiencia, entidade.getDataAudiencia());
            assertEquals(5000.0, entidade.getValorCausa());
            assertNotNull(entidade.getDataHoraInicio());
            assertNotNull(entidade.getUltimaAtualizacao());
        }
    }

    @Nested
    @DisplayName("toUpdateEntity")
    class ToUpdateEntityTest {

        @Test
        @DisplayName("Deve mapear ProcessoAppUpdateRequest para entidade incluindo status e justificativa")
        void deveMapearUpdateRequestParaEntidade() {
            LocalDateTime dataAudiencia = LocalDateTime.of(2026, 12, 1, 14, 0);
            ProcessoAppUpdateRequest request = new ProcessoAppUpdateRequest(
                    "1111111-11.2026.8.26.0001",
                    "Nova descrição",
                    "Nova observação",
                    2L,
                    StatusProcesso.SUSPENSO,
                    3L,
                    dataAudiencia,
                    7500.0,
                    1,
                    "Justificativa de suspensão",
                    null
            );

            ProcessoApp entidade = ProcessoAppMapper.toUpdateEntity(request);

            assertNotNull(entidade);
            assertEquals("Nova descrição", entidade.getDescricaoCaso());
            assertEquals("Nova observação", entidade.getObservacao());
            assertEquals(StatusProcesso.SUSPENSO, entidade.getStatus());
            assertEquals("Justificativa de suspensão", entidade.getJustificativa());
            assertEquals(dataAudiencia, entidade.getDataAudiencia());
            assertEquals(7500.0, entidade.getValorCausa());
            assertNotNull(entidade.getUltimaAtualizacao());
        }
    }

    @Nested
    @DisplayName("toCreateResponse")
    class ToCreateResponseTest {

        @Test
        @DisplayName("Deve retornar null quando processoApp for nulo")
        void deveRetornarNullQuandoProcessoAppNulo() {
            assertNull(ProcessoAppMapper.toCreateResponse(null));
        }

        @Test
        @DisplayName("Deve mapear entidade completa para ProcessoAppCreateResponse")
        void deveMapearEntidadeCompletaParaCreateResponse() {
            ProcessoApp processo = criarProcessoCompleto(StatusProcesso.ENCERRADO);

            ProcessoAppCreateResponse response = ProcessoAppMapper.toCreateResponse(processo);

            assertNotNull(response);
            assertEquals(10L, response.id());
            assertEquals(20L, response.usuarioResponsavelId());
            assertEquals("Advogado Responsável", response.usuarioResponsavelNome());
            assertEquals(30L, response.clienteId());
            assertEquals("Cliente Teste", response.clientNomeRazaoSocial());
            assertEquals("Caso teste", response.descricaoCaso());
            assertEquals("Obs teste", response.obervacao());
            assertEquals(StatusProcesso.ENCERRADO, response.status());
            assertEquals(processo.getEncerradoSuspensoEm(), response.encerradoSuspensoEm());
            assertEquals(20L, response.encerradoSuspensoPorUsuarioId());
            assertEquals("Advogado Responsável", response.encerradoSuspensoPorUsuarioNome());
            assertEquals(1, response.motivoStatusId());
            assertEquals("TRANSITO_EM_JULGADO", response.motivoStatusNome());
            assertEquals("Justificativa do encerramento", response.justificativa());
            assertEquals(5000.0, response.valorCausa());
        }

        @Test
        @DisplayName("Deve mapear entidade com campos opcionais nulos")
        void deveMapearEntidadeComCamposOpcionaisNulos() {
            ProcessoApp processo = new ProcessoApp();
            processo.setId(10L);
            Cliente cliente = new Cliente();
            cliente.setId(30L);
            cliente.setNomeRazaoSocial("Cliente");
            processo.setCliente(cliente);
            processo.setStatus(StatusProcesso.ATIVO);

            ProcessoAppCreateResponse response = ProcessoAppMapper.toCreateResponse(processo);

            assertNotNull(response);
            assertEquals(10L, response.id());
            assertNull(response.usuarioResponsavelId());
            assertNull(response.usuarioResponsavelNome());
            assertNull(response.prioridade());
            assertNull(response.faseProcesso());
            assertNull(response.siglaTribunal());
            assertNull(response.encerradoSuspensoEm());
            assertNull(response.encerradoSuspensoPorUsuarioId());
            assertNull(response.encerradoSuspensoPorUsuarioNome());
            assertNull(response.motivoStatusId());
            assertNull(response.motivoStatusNome());
            assertNull(response.justificativa());
            assertNull(response.processoApi());
        }
    }

    @Nested
    @DisplayName("toUpdateResponse")
    class ToUpdateResponseTest {

        @Test
        @DisplayName("Deve retornar null quando processoApp for nulo")
        void deveRetornarNullQuandoProcessoAppNulo() {
            assertNull(ProcessoAppMapper.toUpdateResponse(null));
        }

        @Test
        @DisplayName("Deve mapear entidade completa para ProcessoAppUpdateResponse")
        void deveMapearEntidadeCompletaParaUpdateResponse() {
            ProcessoApp processo = criarProcessoCompleto(StatusProcesso.SUSPENSO);

            ProcessoAppUpdateResponse response = ProcessoAppMapper.toUpdateResponse(processo);

            assertNotNull(response);
            assertEquals(10L, response.id());
            assertEquals(20L, response.usuarioResponsavelId());
            assertEquals("Advogado Responsável", response.usuarioResponsavelNome());
            assertEquals(30L, response.clienteId());
            assertEquals("Cliente Teste", response.clientNomeRazaoSocial());
            assertEquals(StatusProcesso.SUSPENSO, response.status());
            assertEquals(processo.getEncerradoSuspensoEm(), response.encerradoSuspensoEm());
            assertEquals(20L, response.encerradoSuspensoPorUsuarioId());
            assertEquals("Advogado Responsável", response.encerradoSuspensoPorUsuarioNome());
            assertEquals(1, response.motivoStatusId());
            assertEquals("TRANSITO_EM_JULGADO", response.motivoStatusNome());
            assertEquals("Justificativa do encerramento", response.justificativa());
        }

        @Test
        @DisplayName("Deve mapear entidade com campos opcionais nulos")
        void deveMapearEntidadeComCamposOpcionaisNulos() {
            ProcessoApp processo = new ProcessoApp();
            processo.setId(10L);
            Cliente cliente = new Cliente();
            cliente.setId(30L);
            cliente.setNomeRazaoSocial("Cliente");
            processo.setCliente(cliente);
            processo.setStatus(StatusProcesso.ATIVO);

            ProcessoAppUpdateResponse response = ProcessoAppMapper.toUpdateResponse(processo);

            assertNotNull(response);
            assertEquals(10L, response.id());
            assertNull(response.usuarioResponsavelId());
            assertNull(response.encerradoSuspensoEm());
            assertNull(response.encerradoSuspensoPorUsuarioId());
            assertNull(response.motivoStatusId());
        }
    }

    @Nested
    @DisplayName("toMiniResponse")
    class ToMiniResponseTest {

        @Test
        @DisplayName("Deve retornar null quando processoApp for nulo")
        void deveRetornarNullQuandoProcessoAppNulo() {
            assertNull(ProcessoAppMapper.toMiniResponse(null));
        }

        @Test
        @DisplayName("Deve mapear entidade para ProcessoAppMiniResponse")
        void deveMapearEntidadeParaMiniResponse() {
            ProcessoApp processo = criarProcessoCompleto(StatusProcesso.ATIVO);

            ProcessoAppMiniResponse response = ProcessoAppMapper.toMiniResponse(processo);

            assertNotNull(response);
            assertEquals(10L, response.id());
            assertEquals(30L, response.clienteId());
            assertEquals("Cliente Teste", response.clientNomeRazaoSocial());
            assertEquals(StatusProcesso.ATIVO, response.status());
        }

        @Test
        @DisplayName("Deve mapear para ProcessoAppMiniResponse quando cliente for nulo")
        void deveMapearQuandoClienteNulo() {
            ProcessoApp processo = new ProcessoApp();
            processo.setId(1L);
            processo.setStatus(StatusProcesso.ATIVO);

            ProcessoAppMiniResponse response = ProcessoAppMapper.toMiniResponse(processo);

            assertNotNull(response);
            assertEquals(1L, response.id());
            assertNull(response.clienteId());
            assertNull(response.clientNomeRazaoSocial());
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponseTest {

        @Test
        @DisplayName("Deve mapear entidade completa com histórico para ProcessoAppResponse")
        void deveMapearEntidadeCompletaParaResponse() {
            ProcessoApp processo = criarProcessoCompleto(StatusProcesso.ENCERRADO);
            HistoricoProcessoResponse historico = new HistoricoProcessoResponse(
                    100L, 10L, 20L, "Advogado Responsável",
                    "Alteração de status", LocalDateTime.now(), 1L, "ALTERACAO"
            );

            ProcessoAppResponse response = ProcessoAppMapper.toResponse(processo, List.of(historico));

            assertNotNull(response);
            assertEquals(10L, response.id());
            assertEquals(StatusProcesso.ENCERRADO, response.status());
            assertEquals(1, response.motivoStatusId());
            assertEquals("TRANSITO_EM_JULGADO", response.motivoStatusNome());
            assertEquals("Justificativa do encerramento", response.justificativa());
            assertEquals(1, response.historicoProcessoResponses().size());
        }
    }

    private ProcessoApp criarProcessoCompleto(StatusProcesso status) {
        Usuario usuario = new Usuario();
        usuario.setId(20L);
        usuario.setNome("Advogado Responsável");

        Cliente cliente = new Cliente();
        cliente.setId(30L);
        cliente.setNomeRazaoSocial("Cliente Teste");

        Prioridade prioridade = new Prioridade(1L, "ALTA");
        FaseProcesso fase = new FaseProcesso(1L, "INICIAL");
        SiglaTribunal siglaTribunal = new SiglaTribunal(1L, "TJSP");

        MotivoStatusProcesso motivo = new MotivoStatusProcesso();
        motivo.setId(1);
        motivo.setNome("TRANSITO_EM_JULGADO");
        motivo.setAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO);

        ProcessoApp processo = new ProcessoApp();
        processo.setId(10L);
        processo.setUsuarioResponsavel(usuario);
        processo.setCliente(cliente);
        processo.setDescricaoCaso("Caso teste");
        processo.setObservacao("Obs teste");
        processo.setPrioridade(prioridade);
        processo.setFaseProcesso(fase);
        processo.setUltimaAtualizacao(LocalDateTime.now());
        processo.setDataHoraInicio(LocalDateTime.now().minusDays(10));
        processo.setStatus(status);
        processo.setDataAudiencia(LocalDateTime.now().plusDays(5));
        processo.setEncerradoSuspensoEm(LocalDateTime.now());
        processo.setEncerradoSuspensoPorUsuario(usuario);
        processo.setMotivoStatus(motivo);
        processo.setJustificativa("Justificativa do encerramento");
        processo.setSiglaTribunal(siglaTribunal);
        processo.setValorCausa(5000.0);

        return processo;
    }
}
