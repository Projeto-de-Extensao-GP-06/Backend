package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.response.UsuarioListarResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateUsuario {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;

    public UpdateUsuario(
            UsuarioRepository usuarioRepository,
            CargoRepository cargoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
    }

    public UsuarioListarResponse executar(
            UsuarioAtualizacaoRequest request,
            Long id
    ) {

        if (!usuarioRepository.existsById(id)) {
            throw new UsuarioNaoEncontradoException(id);
        }

        Usuario usuarioAtual =
                usuarioRepository.getReferenceById(id);

        Cargo cargo = cargoRepository
                .findById(request.cargoId())
                .orElseThrow(() ->
                        new CargoNaoEncontradoException("Cargo não encontrado: " + request.cargoId())
                );

        Usuario usuarioEditado =
                UsuarioMapper.toEntity(request, cargo);

        usuarioEditado.setId(id);
        usuarioEditado.setCriadoEm(usuarioAtual.getCriadoEm());
        usuarioEditado.setSenha(usuarioAtual.getSenha());

        usuarioRepository.save(usuarioEditado);

        return UsuarioMapper.toListarDto(usuarioEditado);
    }
}