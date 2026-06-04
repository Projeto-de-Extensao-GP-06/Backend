package br.com.sgpj.adapter.database;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioPersistenciaAdapter {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;

    public UsuarioPersistenciaAdapter(UsuarioRepository usuarioRepository, CargoRepository cargoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public Cargo buscarCargoPorId(Long cargoId) {
        return cargoRepository.findById(cargoId)
                .orElseThrow(() -> new CargoNaoEncontradoException("Cargo não encontrado para esse ID: " + cargoId));
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    public Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado para esse endereço de email: " + email));
    }

    public Usuario findByEmailWithPermissoes(String username) {
        return usuarioRepository.findByEmailWithPermissoes(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("Usuário %s não encontrado", username)));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorFuncao(String funcao) {
        return usuarioRepository.findByCargo_NomeIgnoreCase(funcao.trim());
    }

    public List<Usuario> pesquisarPorNomeOuEmail(String pesquisa) {
        String termo = pesquisa.trim();
        return usuarioRepository.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(termo, termo);
    }

    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}

