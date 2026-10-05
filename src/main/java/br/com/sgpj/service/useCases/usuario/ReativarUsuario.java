package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.dto.response.Usuario.UsuarioListarResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioJaAtivoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class ReativarUsuario {

    private final UsuarioRepository usuarioRepository;

    public ReativarUsuario(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioListarResponse executar(Long id) {

        Usuario usuario =
                usuarioRepository.getReferenceById(id);

        if (usuario.isAtivo()) {
            throw new UsuarioJaAtivoException(
                    "Usuário já está ativo."
            );
        }

        usuario.setAtivo(true);

        usuarioRepository.save(usuario);

        return UsuarioMapper.toListarDto(usuario);
    }
}