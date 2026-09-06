package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.UsuarioAutoExclusaoException;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteUsuario {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    public DeleteUsuario(
            UsuarioRepository usuarioRepository,
            CargoRepository cargoRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    }

    public void executar(Long id) {

        String email =
                usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();

        Usuario usuarioLogado =
                usuarioRepository.findByEmail(email);

        if (usuarioLogado.getId().equals(id)) {
            throw new UsuarioAutoExclusaoException(
                    "Não é possível excluir o próprio usuário."
            );
        }

        Usuario usuario =
                usuarioRepository.getReferenceById(id);

        usuario.setAtivo(false);

        Cargo semCargo =
                cargoRepository.findByNome("Sem Cargo");

        usuario.setCargo(semCargo);

        usuarioRepository.save(usuario);
    }
}