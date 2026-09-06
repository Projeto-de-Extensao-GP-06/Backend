package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.dto.request.UsuarioCriacaoRequest;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateUsuario {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;

    public CreateUsuario(
            UsuarioRepository usuarioRepository,
            CargoRepository cargoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
    }

    public void executar(UsuarioCriacaoRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new EmailDuplicadoException(request.email());
        }

        Cargo cargo = cargoRepository
                .findById(request.cargoId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Cargo não encontrado.")
                );

        Usuario novoUsuario =
                UsuarioMapper.toEntity(request, cargo);

        usuarioRepository.save(novoUsuario);
    }
}