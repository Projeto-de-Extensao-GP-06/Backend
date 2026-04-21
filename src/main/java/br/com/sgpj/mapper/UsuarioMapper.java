package br.com.sgpj.mapper;

import br.com.sgpj.dto.*;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class UsuarioMapper {

  private UsuarioMapper() {}

  public static Usuario toEntity(UsuarioCriacaoRequest dto, Cargo cargo) {
    Usuario usuario = new Usuario();
    usuario.setNome(dto.nome());
    usuario.setEmail(dto.email());
    usuario.setSenha(dto.senha());
    usuario.setCriadoEm(LocalDateTime.now());
    usuario.setCargo(cargo);
    return usuario;
  }

  public static Usuario toEntity(UsuarioAtualizacaoRequest dto, Cargo cargo) {
    Usuario usuario = new Usuario();
    usuario.setNome(dto.nome());
    usuario.setEmail(dto.email());
    usuario.setSenha(dto.senha());
    usuario.setCargo(cargo);
    usuario.setAtivo(dto.ativo());
    return usuario;
  }

  public static Usuario toEntity(UsuarioLoginRequest dto) {
    Usuario usuario = new Usuario();
    usuario.setEmail(dto.email());
    usuario.setSenha(dto.senha());
    return usuario;
  }

  public static UsuarioTokenResponse toTokenDto(Usuario usuario, String token) {
    return new UsuarioTokenResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            token);
  }

  /**
   * Mapeia para o DTO de resposta do login — sem o token.
   *
   * <p>O token não pertence ao body: ele é enviado como cookie HttpOnly
   * via {@code Set-Cookie}. Este DTO carrega apenas os dados necessários
   * para o frontend identificar o usuário na sessão.</p>
   */
  public static UsuarioSessaoResponse toSessaoDto(UsuarioTokenResponse tokenDto, Usuario usuario) {
    UsuarioSessaoResponse.CargoDto cargoDto = null;
    List<String> permissoes = Collections.emptyList();

    if (usuario.getCargo() != null) {
      cargoDto = new UsuarioSessaoResponse.CargoDto(
              usuario.getCargo().getId(),
              usuario.getCargo().getNome()
      );

      if (usuario.getCargo().getPermissoes() != null) {
        permissoes = usuario.getCargo().getPermissoes().stream()
                .map(cargoPermissao -> cargoPermissao.getPermissao().getNome())
                .toList();
      }
    }

    return new UsuarioSessaoResponse(
            tokenDto.id(),
            tokenDto.nome(),
            tokenDto.email(),
            cargoDto,
            permissoes
    );
  }

  public static UsuarioListarResponse toListarDto(Usuario usuario) {
    return new UsuarioListarResponse(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getCargo() != null ? usuario.getCargo().getId() : null,
            usuario.getCargo() != null ? usuario.getCargo().getNome() : null,
            usuario.getCriadoEm(),
            usuario.isAtivo());
  }
}
