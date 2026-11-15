package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioAtualizarPerfilRequest;
import br.com.sgpj.dto.response.Usuario.UsuarioListarResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AtualizarMeuPerfil {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    public AtualizarMeuPerfil(
            UsuarioRepository usuarioRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    }

    @Transactional
    public UsuarioListarResponse executar(UsuarioAtualizarPerfilRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Insira um payload válido.");
        }

        String emailLogado = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado);

        if (usuario == null) {
            log.warn("Usuário autenticado com email {} não encontrado.", emailLogado);
            throw new UsuarioNaoEncontradoException(emailLogado);
        }

        if (usuario.isUsuarioSistema()) {
            throw new OperacaoInvalidaException("Não é permitido alterar o usuário do sistema.");
        }

        String novoNome = request.nome().trim();
        String novoEmail = request.email().trim();

        if (!novoEmail.equalsIgnoreCase(usuario.getEmail())
                && usuarioRepository.existsByEmail(novoEmail)) {
            throw new EmailDuplicadoException(novoEmail);
        }

        usuario.setNome(novoNome);
        usuario.setEmail(novoEmail);

        usuarioRepository.save(usuario);

        return UsuarioMapper.toListarDto(usuario);
    }
}
