package br.com.sgpj.service.useCases.onedrive;

import br.com.sgpj.adapter.onedrive.OneDriveTokenAdapter;
import br.com.sgpj.exception.OneDriveErroException;
import com.microsoft.graph.models.DriveItem;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.kiota.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OneDriveGraphGatewayTest {

    private static final String PAI = "root:/sgpj/usuarios:";
    private static final String CAMINHO = "root:/sgpj/usuarios/pasta-salva:";
    private GraphServiceClient cliente;
    private OneDriveGraphGateway gateway;

    @BeforeEach
    void preparar() {
        cliente = mock(GraphServiceClient.class, RETURNS_DEEP_STUBS);
        gateway = spy(new OneDriveGraphGateway(mock(OneDriveTokenAdapter.class)));
        doReturn(cliente).when(gateway).criarCliente();
        doReturn("drive").when(gateway).obterDriveId();
    }

    @Test
    void deveReutilizarPastaExistente() {
        DriveItem pasta = new DriveItem();
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(CAMINHO).get()).thenReturn(pasta);

        assertSame(pasta, gateway.garantirPasta(PAI, "pasta-salva"));

        verify(cliente.drives().byDriveId("drive").items().byDriveItemId(PAI).children(), never()).post(any());
    }

    @Test
    void deveCriarSomenteQuandoAusenteSemSubstituir() {
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(CAMINHO).get())
                .thenThrow(erro(404));
        DriveItem pasta = new DriveItem();
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(PAI).children().post(any()))
                .thenReturn(pasta);

        assertSame(pasta, gateway.garantirPasta(PAI, "pasta-salva"));

        ArgumentCaptor<DriveItem> captor = ArgumentCaptor.forClass(DriveItem.class);
        verify(cliente.drives().byDriveId("drive").items().byDriveItemId(PAI).children()).post(captor.capture());
        assertEquals("pasta-salva", captor.getValue().getName());
        assertNotNull(captor.getValue().getFolder());
        assertEquals("fail", captor.getValue().getAdditionalData().get("@microsoft.graph.conflictBehavior"));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 429, 500, 503})
    void naoDeveCriarEmFalhasQueNaoSejamAusencia(int status) {
        ApiException erro = erro(status);
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(CAMINHO).get()).thenThrow(erro);

        assertSame(erro, assertThrows(OneDriveErroException.class,
                () -> gateway.garantirPasta(PAI, "pasta-salva")).getCause());

        verify(cliente.drives().byDriveId("drive").items().byDriveItemId(PAI).children(), never()).post(any());
    }

    @Test
    void deveRecuperarPastaCriadaPorOutraChamada() {
        DriveItem pasta = new DriveItem();
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(CAMINHO).get())
                .thenThrow(erro(404)).thenReturn(pasta);
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(PAI).children().post(any()))
                .thenThrow(erro(409));

        assertSame(pasta, gateway.garantirPasta(PAI, "pasta-salva"));
        verify(cliente.drives().byDriveId("drive").items().byDriveItemId(CAMINHO), times(2)).get();
    }

    @Test
    void devePropagarFalhaNaCriacao() {
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(CAMINHO).get())
                .thenThrow(erro(404));
        ApiException erro = erro(503);
        when(cliente.drives().byDriveId("drive").items().byDriveItemId(PAI).children().post(any()))
                .thenThrow(erro);

        assertSame(erro, assertThrows(OneDriveErroException.class,
                () -> gateway.garantirPasta(PAI, "pasta-salva")).getCause());
    }

    private ApiException erro(int status) {
        return new GraphTestException(status);
    }

    private static class GraphTestException extends ApiException {
        GraphTestException(int status) {
            setResponseStatusCode(status);
        }
    }
}
