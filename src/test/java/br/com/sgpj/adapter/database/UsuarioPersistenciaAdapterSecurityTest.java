package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioPersistenciaAdapter — Testes de segurança para enumeração de conta")
class UsuarioPersistenciaAdapterSecurityTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CargoRepository cargoRepository;

    @InjectMocks
    private UsuarioPersistenciaAdapter adapter;

    private static final String EMAIL_TESTE = "teste@email.com";
    private static final Long ID_TESTE = 1L;

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException sem expor email em buscarUsuarioPorEmail")
    void deveLancarExcecaoSemExporEmailEmBuscarUsuarioPorEmail() {
        // Given
        when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.empty());

        // When/Then
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.buscarUsuarioPorEmail(EMAIL_TESTE)
        );

        // Verifica que a mensagem não contém o email
        assertFalse(exception.getMessage().contains(EMAIL_TESTE), 
            "Email não deve estar exposto na mensagem de erro");
        assertFalse(exception.getMessage().contains("@"), 
            "Email não deve estar exposto na mensagem de erro");
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository).findByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException sem expor email em findByEmailWithPermissoes")
    void deveLancarExcecaoSemExporEmailEmFindByEmailWithPermissoes() {
        // Given
        when(usuarioRepository.findByEmailWithPermissoes(EMAIL_TESTE)).thenReturn(Optional.empty());

        // When/Then
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.findByEmailWithPermissoes(EMAIL_TESTE)
        );

        // Verifica que a mensagem não contém o email
        assertFalse(exception.getMessage().contains(EMAIL_TESTE), 
            "Email não deve estar exposto na mensagem de erro");
        assertFalse(exception.getMessage().contains("@"), 
            "Email não deve estar exposto na mensagem de erro");
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository).findByEmailWithPermissoes(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException sem expor ID em buscarUsuarioPorId")
    void deveLancarExcecaoSemExporIdEmBuscarUsuarioPorId() {
        // Given
        when(usuarioRepository.findById(ID_TESTE)).thenReturn(Optional.empty());

        // When/Then
        UsuarioNaoEncontradoException exception = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.buscarUsuarioPorId(ID_TESTE)
        );

        // Verifica que a mensagem não contém o ID
        assertFalse(exception.getMessage().contains(ID_TESTE.toString()), 
            "ID não deve estar exposto na mensagem de erro");
        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(usuarioRepository).findById(ID_TESTE);
    }

    @Test
    @DisplayName("Deve retornar mesmo tipo de exceção para email não encontrado - evita enumeração")
    void deveRetornarMesmoTipoExcecaoParaEmailNaoEncontrado() {
        // Given
        when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailWithPermissoes(EMAIL_TESTE)).thenReturn(Optional.empty());

        // When/Then
        UsuarioNaoEncontradoException exception1 = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.buscarUsuarioPorEmail(EMAIL_TESTE)
        );

        UsuarioNaoEncontradoException exception2 = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.findByEmailWithPermissoes(EMAIL_TESTE)
        );

        // Verifica que ambas lançam o mesmo tipo de exceção
        assertEquals(exception1.getClass(), exception2.getClass());
        assertEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    @DisplayName("Deve não diferenciar mensagens de erro entre métodos - evita enumeração")
    void deveNaoDiferenciarMensagensErroEntreMetodos() {
        // Given
        String emailValido = "valido@email.com";
        String emailInvalido = "invalido@email.com";
        
        when(usuarioRepository.findByEmail(emailValido)).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmailWithPermissoes(emailInvalido)).thenReturn(Optional.empty());

        // When/Then
        UsuarioNaoEncontradoException exception1 = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.buscarUsuarioPorEmail(emailValido)
        );

        UsuarioNaoEncontradoException exception2 = assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.findByEmailWithPermissoes(emailInvalido)
        );

        // Verifica que as mensagens são idênticas (não expõem informações diferentes)
        assertEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    @DisplayName("Deve lançar UsuarioNaoEncontradoException ao invés de UsernameNotFoundException")
    void deveLancarUsuarioNaoEncontradoExceptionAoInvésDeUsernameNotFoundException() {
        // Given
        when(usuarioRepository.findByEmailWithPermissoes(EMAIL_TESTE)).thenReturn(Optional.empty());

        // When/Then
        // Verifica que lança UsuarioNaoEncontradoException e não UsernameNotFoundException
        assertThrows(
                UsuarioNaoEncontradoException.class,
                () -> adapter.findByEmailWithPermissoes(EMAIL_TESTE)
        );
    }

    @Test
    @DisplayName("Deve retornar usuário quando encontrado em buscarUsuarioPorEmail")
    void deveRetornarUsuarioQuandoEncontradoEmBuscarUsuarioPorEmail() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL_TESTE);
        when(usuarioRepository.findByEmail(EMAIL_TESTE)).thenReturn(Optional.of(usuario));

        // When
        Usuario resultado = adapter.buscarUsuarioPorEmail(EMAIL_TESTE);

        // Then
        assertNotNull(resultado);
        assertEquals(EMAIL_TESTE, resultado.getEmail());
        verify(usuarioRepository).findByEmail(EMAIL_TESTE);
    }

    @Test
    @DisplayName("Deve retornar usuário quando encontrado em findByEmailWithPermissoes")
    void deveRetornarUsuarioQuandoEncontradoEmFindByEmailWithPermissoes() {
        // Given
        Usuario usuario = new Usuario();
        usuario.setEmail(EMAIL_TESTE);
        when(usuarioRepository.findByEmailWithPermissoes(EMAIL_TESTE)).thenReturn(Optional.of(usuario));

        // When
        Usuario resultado = adapter.findByEmailWithPermissoes(EMAIL_TESTE);

        // Then
        assertNotNull(resultado);
        assertEquals(EMAIL_TESTE, resultado.getEmail());
        verify(usuarioRepository).findByEmailWithPermissoes(EMAIL_TESTE);
    }
}
