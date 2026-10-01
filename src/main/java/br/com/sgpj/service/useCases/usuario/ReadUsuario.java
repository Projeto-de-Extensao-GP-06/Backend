package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ReadUsuario {

    private final UsuarioRepository usuarioRepository;

    public ReadUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioListarResponse> listarTodos() {

        return usuarioRepository
                .findAll()
                .stream()
                .map(UsuarioMapper::toListarDto)
                .toList();
    }

    public List<UsuarioListarResponse> listarPorCargo(String cargo) {

        return usuarioRepository
                .findAllByCargoNome(cargo)
                .stream()
                .map(UsuarioMapper::toListarDto)
                .toList();
    }

    public List<UsuarioListarResponse> buscarPorNomeOuEmail(
            String pesquisa
    ) {

        return usuarioRepository
                .findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        pesquisa,
                        pesquisa
                )
                .stream()
                .map(UsuarioMapper::toListarDto)
                .toList();
    }

    public Usuario obterPorId(Long id) {
        if (!usuarioRepository.existsById(id)) {
            log.warn("Usuário com ID {} não encontrado.", id);
            throw new UsuarioNaoEncontradoException(id);
        }
        return usuarioRepository.getReferenceById(id);
    }
}