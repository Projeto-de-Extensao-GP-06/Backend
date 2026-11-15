package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioAtualizacaoRequest;
import br.com.sgpj.dto.response.Usuario.UsuarioListarResponse;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.exception.OperacaoInvalidaException;
import br.com.sgpj.exception.UltimoAdministradorException;
import br.com.sgpj.exception.UsuarioAutoExclusaoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UpdateUsuario {

    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;

    public UpdateUsuario(
            UsuarioRepository usuarioRepository,
            CargoRepository cargoRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter
    ) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
    }

    @Transactional
    public UsuarioListarResponse executar(
            UsuarioAtualizacaoRequest request,
            Long id
    ) {
        String emailLogado = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
        Usuario usuarioLogado = usuarioRepository.findByEmail(emailLogado);

        if (usuarioLogado == null) {
            log.warn("Usuário autenticado com email {} não encontrado.", emailLogado);
            throw new UsuarioNaoEncontradoException(emailLogado);
        }

        if (usuarioLogado.getId().equals(id)) {
            if (!request.ativo()) {
                throw new UsuarioAutoExclusaoException(
                        "Você não pode desativar sua própria conta. Peça a outro administrador."
                );
            }
            if (usuarioLogado.getCargo() != null
                    && !usuarioLogado.getCargo().getId().equals(request.cargoId())) {
                throw new UsuarioAutoExclusaoException(
                        "Você não pode alterar seu próprio cargo. Peça a outro administrador."
                );
            }
            throw new UsuarioAutoExclusaoException(
                    "Para alterar seus dados pessoais (nome e e-mail), utilize a tela de Meu Perfil."
            );
        }

        if (!usuarioRepository.existsById(id)) {
            log.warn("Usuário com ID {} não encontrado.", id);
            throw new UsuarioNaoEncontradoException(id);
        }

        Usuario usuarioAtual =
                usuarioRepository.getReferenceById(id);

        if (usuarioAtual.isUsuarioSistema()) {
            throw new OperacaoInvalidaException("Não é permitido alterar o usuário do sistema.");
        }

        if (request.email() != null
                && !request.email().trim().equalsIgnoreCase(usuarioAtual.getEmail())
                && usuarioRepository.existsByEmail(request.email().trim())) {
            throw new EmailDuplicadoException(request.email().trim());
        }

        Cargo cargo = cargoRepository
                .findById(request.cargoId())
                .orElseThrow(() ->
                        new CargoNaoEncontradoException("Cargo não encontrado: " + request.cargoId())
                );

        boolean eraAdminAtivo = AdministradorValidator.isUsuarioAdministradorAtivo(usuarioAtual);
        boolean continuaraAdminAtivo = request.ativo()
                && AdministradorValidator.isCargoAdministrador(cargo);

        if (eraAdminAtivo
                && !continuaraAdminAtivo
                && !AdministradorValidator.existeOutroAdministradorAtivo(usuarioRepository, id)) {
            if (!request.ativo()) {
                throw new UltimoAdministradorException(
                        "Não é possível desativar o único administrador ativo do sistema."
                );
            }
            throw new UltimoAdministradorException(
                    "Não é possível alterar o cargo do único administrador ativo do sistema."
            );
        }

        Usuario usuarioEditado =
                UsuarioMapper.toEntity(request, cargo);

        usuarioEditado.setId(id);
        usuarioEditado.setCriadoEm(usuarioAtual.getCriadoEm());
        usuarioEditado.setSenha(usuarioAtual.getSenha());

        usuarioRepository.save(usuarioEditado);

        return UsuarioMapper.toListarDto(usuarioEditado);
    }
}
