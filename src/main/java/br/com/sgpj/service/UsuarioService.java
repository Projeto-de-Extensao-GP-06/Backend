package br.com.sgpj.service;

import br.com.sgpj.config.jwt.GerenciadorTokenJwt;
import br.com.sgpj.dto.UsuarioCriacaoDto;
import br.com.sgpj.dto.UsuarioListarDto;
import br.com.sgpj.dto.UsuarioTokenDto;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.exception.EmailDuplicadoException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.mapper.UsuarioMapper;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

  private final PasswordEncoder passwordEncoder;
  private final UsuarioRepository usuarioRepository;
  private final GerenciadorTokenJwt gerenciadorTokenJwt;
  private final AuthenticationManager authenticationManager;
  private final CargoRepository cargoRepository;

  public UsuarioService(
          PasswordEncoder passwordEncoder,
          UsuarioRepository usuarioRepository,
          GerenciadorTokenJwt gerenciadorTokenJwt,
          AuthenticationManager authenticationManager,
          CargoRepository cargoRepository) {
    this.passwordEncoder = passwordEncoder;
    this.usuarioRepository = usuarioRepository;
    this.gerenciadorTokenJwt = gerenciadorTokenJwt;
    this.authenticationManager = authenticationManager;
    this.cargoRepository = cargoRepository;
  }

  public void criar(UsuarioCriacaoDto request) {
    if (usuarioRepository.existsByEmail(request.email())) {
      throw new EmailDuplicadoException(request.email());
    }

    Optional<Cargo> cargo = cargoRepository.findById(request.cargoId());

    if (cargo.isEmpty()) {
      throw new CargoNaoEncontradoException("Cargo não encontrado para esse ID: " + request.cargoId());
    }

    Usuario novoUsuario = UsuarioMapper.toEntity(request, cargo.get());
    String senhaCriptografada = passwordEncoder.encode(novoUsuario.getSenha());
    novoUsuario.setSenha(senhaCriptografada);
    usuarioRepository.save(novoUsuario);
  }

  public UsuarioTokenDto autenticar(Usuario usuario) {
    final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
          usuario.getEmail(), usuario.getSenha());

    final Authentication authentication = this.authenticationManager.authenticate(credentials);

    Usuario usuarioAutenticado = usuarioRepository.findByEmail(usuario.getEmail())
            .orElseThrow(() -> new UsuarioNaoEncontradoException(usuario.getEmail()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    final String token = gerenciadorTokenJwt.generateToken(authentication);

    return UsuarioMapper.toTokenDto(usuarioAutenticado, token);
  }

  public List<UsuarioListarDto> listarTodos() {
    return usuarioRepository.findAll().stream().map(UsuarioMapper::toListarDto).toList();
  }

  public Usuario obterPorId(Long id) {
    return usuarioRepository.findById(id)
            .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário com ID " + id + " não encontrado"));
  }
}