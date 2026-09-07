package br.com.sgpj.service.useCases.cargo;

import br.com.sgpj.dto.request.CriarCargoRequest;
import br.com.sgpj.dto.response.CargoResponse;
import br.com.sgpj.dto.response.PermissaoResponse;
import br.com.sgpj.entity.*;
import br.com.sgpj.exception.CargoJaExisteException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.CargoPermissaoRepository;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.PermissaoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CreateCargo {

    public final CargoRepository cargoRepository;
    public final UsuarioRepository usuarioRepository;
    public final PermissaoRepository permissaoRepository;
    public final CargoPermissaoRepository cargoPermissaoRepository;

    public CreateCargo(CargoRepository cargoRepository, UsuarioRepository usuarioRepository, PermissaoRepository permissaoRepository, CargoPermissaoRepository cargoPermissaoRepository) {
        this.cargoRepository = cargoRepository;
        this.usuarioRepository = usuarioRepository;
        this.permissaoRepository = permissaoRepository;
        this.cargoPermissaoRepository = cargoPermissaoRepository;
    }

    public CargoResponse criarCargo(CriarCargoRequest request, String emailUsuario){
        if (cargoRepository.existsByNome(request.nome())) {
            throw new CargoJaExisteException("O cargo " + request.nome() + " já existe");
        }

        Usuario criadoPor = usuarioRepository.findByEmail(emailUsuario);
        if (criadoPor == null) {
            throw new UsuarioNaoEncontradoException(emailUsuario);
        }
        Permissao semPermissoes = permissaoRepository.getReferenceById(ConstantesValidation.SEM_PERMISSOES_ID);

        Cargo cargo = new Cargo();
        cargo.setNome(request.nome());
        cargo.setCriadoPor(criadoPor);
        cargo.setCriadoEm(LocalDateTime.now());

        Cargo cargoSalvo = cargoRepository.save(cargo);

        cargoPermissaoRepository.save(new CargoPermissao(
                new CargoPermissaoId(cargoSalvo.getId(), ConstantesValidation.SEM_PERMISSOES_ID),
                cargoSalvo,
                semPermissoes,
                criadoPor,
                LocalDateTime.now()
        ));

        return new CargoResponse(cargoSalvo.getId(), cargoSalvo.getNome(),
                List.of(new PermissaoResponse(semPermissoes.getId(), semPermissoes.getNome())));
    }
}
