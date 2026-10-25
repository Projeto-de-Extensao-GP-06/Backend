package br.com.sgpj.service.useCases.solicitacaoDuvida;

import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.StatusClienteEnum;
import br.com.sgpj.repository.ProcessoAppRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarSolicitacoesPendentesTest {

    @Mock
    private ProcessoAppRepository processoAppRepository;

    @InjectMocks
    private ListarSolicitacoesPendentes listarSolicitacoesPendentes;

    @Test
    void deveRepassarPageableAoRepositoryEPreservarMetadados() {
        Pageable pageable = PageRequest.of(1, 2);
        Page<ProcessoApp> processos = new PageImpl<>(List.of(), pageable, 3);
        when(processoAppRepository.findByCliente_StatusCliente_NomeIgnoreCase(
                StatusClienteEnum.LEAD.getNome(), pageable)).thenReturn(processos);

        Page<?> resultado = listarSolicitacoesPendentes.listarPendentes(pageable);

        assertEquals(1, resultado.getNumber());
        assertEquals(2, resultado.getSize());
        assertEquals(3, resultado.getTotalElements());
        assertEquals(2, resultado.getTotalPages());
        verify(processoAppRepository).findByCliente_StatusCliente_NomeIgnoreCase(
                StatusClienteEnum.LEAD.getNome(), pageable);
    }
}
