package br.com.sgpj.mapper;

import br.com.sgpj.dto.UsuarioCriacaoDto;
import br.com.sgpj.dto.UsuarioListarDto;
import br.com.sgpj.dto.UsuarioLoginDto;
import br.com.sgpj.dto.UsuarioSessaoDto;
import br.com.sgpj.dto.UsuarioTokenDto;
import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class UsuarioMapper {

  private UsuarioMapper() {}

  public static Usuario toEntity(UsuarioCriacaoDto dto, Cargo cargo) {
    Usuario usuario = new Usuario();
    usuario.setNome(dto.nome());
    usuario.setEmail(dto.email());
    usuario.setSenha(dto.senha());
    usuario.setCriadoEm(LocalDateTime.now());
    usuario.setCargo(cargo);
    return usuario;
  }

  public static Usuario toEntity(UsuarioLoginDto dto) {
    Usuario usuario = new Usuario();
    usuario.setEmail(dto.email());
    usuario.setSenha(dto.senha());
    return usuario;
  }

  public static UsuarioTokenDto toTokenDto(Usuario usuario, String token) {
    return new UsuarioTokenDto(
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
  public static UsuarioSessaoDto toSessaoDto(UsuarioTokenDto tokenDto, Usuario usuario) {
    UsuarioSessaoDto.CargoDto cargoDto = null;
    List<String> permissoes = Collections.emptyList();

    if (usuario.getCargo() != null) {
      cargoDto = new UsuarioSessaoDto.CargoDto(
              usuario.getCargo().getId(),
              usuario.getCargo().getNome()
      );

      if (usuario.getCargo().getPermissoes() != null) {
        permissoes = usuario.getCargo().getPermissoes().stream()
                .map(cargoPermissao -> cargoPermissao.getPermissao().getNome())
                .toList();
      }
    }

    return new UsuarioSessaoDto(
            tokenDto.id(),
            tokenDto.nome(),
            tokenDto.email(),
            cargoDto,
            permissoes
    );
  }

  public static UsuarioListarDto toListarDto(Usuario usuario) {
    return new UsuarioListarDto(
            usuario.getId(),
            usuario.getNome(),
            usuario.getEmail(),
            usuario.getCargo() != null ? usuario.getCargo().getId() : null,
            usuario.getCargo() != null ? usuario.getCargo().getNome() : null,
            usuario.getCriadoEm(),
            usuario.isAtivo());
  }
}
