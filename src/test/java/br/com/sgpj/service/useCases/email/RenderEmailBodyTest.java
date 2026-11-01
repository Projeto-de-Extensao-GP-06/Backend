package br.com.sgpj.service.useCases.email;

import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApi;
import br.com.sgpj.entity.Processo.ProcessoApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do RenderEmailBody")
class RenderEmailBodyTest {

    private RenderEmailBody renderEmailBody;

    @BeforeEach
    void setUp() {
        renderEmailBody = new RenderEmailBody();
    }

    @Test
    @DisplayName("Deve renderizar o e-mail semanal com lista de audiências, datas formatadas e dados essenciais")
    void deveRenderizarEmailSemanalDeAudiencias() {
        LocalDate inicioSemana = LocalDate.of(2026, 10, 25);
        LocalDate fimSemana = LocalDate.of(2026, 10, 31);

        ProcessoApp p1 = criarProcesso(
                LocalDateTime.of(2026, 10, 26, 14, 30),
                "João Carlos da Silva",
                "1000001-12.2026.8.26.0100"
        );
        ProcessoApp p2 = criarProcesso(
                LocalDateTime.of(2026, 10, 28, 9, 0),
                "Empresa X & Filhos <LTDA>",
                null
        );

        RenderEmailBody.EmailContent content = renderEmailBody.renderWeeklyHearings(
                "Dra. Thauany", inicioSemana, fimSemana, List.of(p1, p2)
        );

        assertEquals("Resumo semanal de audiências - SGPJ", content.subject());
        assertTrue(content.body().contains("Dra. Thauany"));
        assertTrue(content.body().contains("2 audiência(s)"));
        assertTrue(content.body().contains("25/10/2026 a 31/10/2026"));
        assertTrue(content.body().contains("26/10/2026 às 14:30"));
        assertTrue(content.body().contains("João Carlos da Silva"));
        assertTrue(content.body().contains("1000001-12.2026.8.26.0100"));
        assertTrue(content.body().contains("28/10/2026 às 09:00"));
        assertTrue(content.body().contains("Empresa X &amp; Filhos &lt;LTDA&gt;"));
        assertTrue(content.body().contains("Não informado"));
    }

    @Test
    @DisplayName("Deve renderizar o e-mail diário com lista de audiências, data do dia e dados essenciais")
    void deveRenderizarEmailDiarioDeAudiencias() {
        LocalDate dataReferencia = LocalDate.of(2026, 10, 26);

        ProcessoApp p1 = criarProcesso(
                LocalDateTime.of(2026, 10, 26, 10, 15),
                "Mariana Oliveira Santos",
                "2000002-55.2026.8.26.0100"
        );

        RenderEmailBody.EmailContent content = renderEmailBody.renderDailyHearings(
                "Dr. Rafael", dataReferencia, List.of(p1)
        );

        assertEquals("Lembrete de audiências do dia - SGPJ", content.subject());
        assertTrue(content.body().contains("Dr. Rafael"));
        assertTrue(content.body().contains("1 audiência(s)"));
        assertTrue(content.body().contains("26/10/2026"));
        assertTrue(content.body().contains("26/10/2026 às 10:15"));
        assertTrue(content.body().contains("Mariana Oliveira Santos"));
        assertTrue(content.body().contains("2000002-55.2026.8.26.0100"));
    }

    private ProcessoApp criarProcesso(LocalDateTime dataAudiencia, String nomeCliente, String numeroCnj) {
        Cliente cliente = new Cliente();
        cliente.setNomeRazaoSocial(nomeCliente);

        ProcessoApp processo = new ProcessoApp();
        processo.setDataAudiencia(dataAudiencia);
        processo.setCliente(cliente);

        if (numeroCnj != null) {
            ProcessoApi api = new ProcessoApi();
            api.setNumeroCnj(numeroCnj);
            processo.setProcessoApi(api);
        }
        return processo;
    }
}
