package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.exception.UltimoAdministradorException;
import br.com.sgpj.exception.UsuarioAutoExclusaoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DeleteUsuario {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    public DeleteUsuario(
            UsuarioRepository usuarioRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    }

    @Transactional
    public void executar(Long id) {

        String email =
                usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();

        Usuario usuarioLogado =
                usuarioRepository.findByEmail(email);

        if (usuarioLogado == null) {
            log.warn("Usuário com email {} não encontrado.", email);
            throw new UsuarioNaoEncontradoException(email);
        }

        if (usuarioLogado.getId().equals(id)) {
            throw new UsuarioAutoExclusaoException(
                    "Você não pode desativar ou excluir sua própria conta. Peça a outro administrador."
            );
        }

        if (!usuarioRepository.existsById(id)) {
            log.warn("Usuário com ID {} não encontrado.", id);
            throw new UsuarioNaoEncontradoException(id);
        }

        Usuario usuario =
                usuarioRepository.getReferenceById(id);

        if (usuario.isUsuarioSistema()) {
            throw new OperacaoInvalidaException("Não é permitido excluir o usuário do sistema.");
        }

        if (AdministradorValidator.isUsuarioAdministradorAtivo(usuario)
                && !AdministradorValidator.existeOutroAdministradorAtivo(usuarioRepository, id)) {
            throw new UltimoAdministradorException(
                    "Não é possível desativar ou excluir o único administrador ativo do sistema."
            );
        }

        usuario.setAtivo(false);

        usuarioRepository.save(usuario);
    }
}
