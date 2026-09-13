package br.com.sgpj.service;

import br.com.sgpj.entity.Processo.MotivoStatusProcesso;
import br.com.sgpj.repository.MotivoStatusProcessoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MotivoStatusProcessoService")
class MotivoStatusProcessoServiceTest {

    @Mock
    private MotivoStatusProcessoRepository motivoStatusProcessoRepository;

    @InjectMocks
    private MotivoStatusProcessoService motivoStatusProcessoService;

    @Nested
    @DisplayName("listarTodos")
    class ListarTodosTest {

        @Test
        @DisplayName("Deve retornar todos os motivos de status cadastrados")
        void deveRetornarTodosOsMotivos() {
            MotivoStatusProcesso motivo1 = new MotivoStatusProcesso();
            motivo1.setId(1);
            motivo1.setNome("ACORDO_ENTRE_PARTES");
            motivo1.setAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO);

            MotivoStatusProcesso motivo2 = new MotivoStatusProcesso();
            motivo2.setId(2);
            motivo2.setNome("AGUARDANDO_DECISAO_SUPERIOR");
            motivo2.setAplicavelA(MotivoStatusProcesso.AplicavelA.SUSPENSO);

            when(motivoStatusProcessoRepository.findAll()).thenReturn(List.of(motivo1, motivo2));

            List<MotivoStatusProcesso> resultado = motivoStatusProcessoService.listarTodos();

            assertNotNull(resultado);
            assertEquals(2, resultado.size());
            assertEquals("ACORDO_ENTRE_PARTES", resultado.get(0).getNome());
            assertEquals("AGUARDANDO_DECISAO_SUPERIOR", resultado.get(1).getNome());
            verify(motivoStatusProcessoRepository).findAll();
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não existirem motivos")
        void deveRetornarListaVaziaQuandoNaoExistirMotivos() {
            when(motivoStatusProcessoRepository.findAll()).thenReturn(List.of());

            List<MotivoStatusProcesso> resultado = motivoStatusProcessoService.listarTodos();

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(motivoStatusProcessoRepository).findAll();
        }
    }

    @Nested
    @DisplayName("listarPorAplicabilidade")
    class ListarPorAplicabilidadeTest {

        @Test
        @DisplayName("Deve retornar motivos filtrados por aplicabilidade SUSPENSO")
        void deveRetornarMotivosPorAplicabilidadeSuspenso() {
            MotivoStatusProcesso motivo = new MotivoStatusProcesso();
            motivo.setId(1);
            motivo.setNome("AGUARDANDO_JULGAMENTO_TEMA_REPETITIVO");
            motivo.setAplicavelA(MotivoStatusProcesso.AplicavelA.SUSPENSO);

            when(motivoStatusProcessoRepository.findByAplicavelA(MotivoStatusProcesso.AplicavelA.SUSPENSO))
                    .thenReturn(List.of(motivo));

            List<MotivoStatusProcesso> resultado = motivoStatusProcessoService
                    .listarPorAplicabilidade(MotivoStatusProcesso.AplicavelA.SUSPENSO);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(MotivoStatusProcesso.AplicavelA.SUSPENSO, resultado.get(0).getAplicavelA());
            verify(motivoStatusProcessoRepository).findByAplicavelA(MotivoStatusProcesso.AplicavelA.SUSPENSO);
        }

        @Test
        @DisplayName("Deve retornar motivos filtrados por aplicabilidade ENCERRADO")
        void deveRetornarMotivosPorAplicabilidadeEncerrado() {
            MotivoStatusProcesso motivo = new MotivoStatusProcesso();
            motivo.setId(2);
            motivo.setNome("TRANSITO_EM_JULGADO");
            motivo.setAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO);

            when(motivoStatusProcessoRepository.findByAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO))
                    .thenReturn(List.of(motivo));

            List<MotivoStatusProcesso> resultado = motivoStatusProcessoService
                    .listarPorAplicabilidade(MotivoStatusProcesso.AplicavelA.ENCERRADO);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(MotivoStatusProcesso.AplicavelA.ENCERRADO, resultado.get(0).getAplicavelA());
            verify(motivoStatusProcessoRepository).findByAplicavelA(MotivoStatusProcesso.AplicavelA.ENCERRADO);
        }

        @Test
        @DisplayName("Deve retornar motivos filtrados por aplicabilidade AMBOS")
        void deveRetornarMotivosPorAplicabilidadeAmbos() {
            MotivoStatusProcesso motivo = new MotivoStatusProcesso();
            motivo.setId(3);
            motivo.setNome("OUTROS");
            motivo.setAplicavelA(MotivoStatusProcesso.AplicavelA.AMBOS);

            when(motivoStatusProcessoRepository.findByAplicavelA(MotivoStatusProcesso.AplicavelA.AMBOS))
                    .thenReturn(List.of(motivo));

            List<MotivoStatusProcesso> resultado = motivoStatusProcessoService
                    .listarPorAplicabilidade(MotivoStatusProcesso.AplicavelA.AMBOS);

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            assertEquals(MotivoStatusProcesso.AplicavelA.AMBOS, resultado.get(0).getAplicavelA());
            verify(motivoStatusProcessoRepository).findByAplicavelA(MotivoStatusProcesso.AplicavelA.AMBOS);
        }
    }
}
