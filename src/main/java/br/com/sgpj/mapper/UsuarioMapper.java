package br.com.sgpj.mapper;

import br.com.sgpj.dto.UsuarioCriacaoDto;
import br.com.sgpj.dto.UsuarioListarDto;
import br.com.sgpj.dto.UsuarioLoginDto;
import br.com.sgpj.dto.UsuarioSessaoDto;
import br.com.sgpj.dto.UsuarioTokenDto;
import br.com.sgpj.entity.Usuario;

public class UsuarioMapper {

  private UsuarioMapper() {}

  public static Usuario toEntity(UsuarioCriacaoDto dto) {
    return new Usuario(null, dto.nome(), dto.email(), dto.senha());
  }

  public static Usuario toEntity(UsuarioLoginDto dto) {
    return new Usuario(null, null, dto.email(), dto.senha());
  }

  public static UsuarioTokenDto toTokenDto(Usuario usuario, String token) {
    return new UsuarioTokenDto(usuario.getId(), usuario.getNome(), usuario.getEmail(), token);
  }

  /**
   * Mapeia para o DTO de resposta do login — sem o token.
   *
   * <p>O token não pertence ao body: ele é enviado como cookie HttpOnly
   * via {@code Set-Cookie}. Este DTO carrega apenas os dados necessários
   * para o frontend identificar o usuário na sessão.</p>
   */
  public static UsuarioSessaoDto toSessaoDto(UsuarioTokenDto tokenDto) {
    return new UsuarioSessaoDto(tokenDto.userId(), tokenDto.nome(), tokenDto.email());
  }

  public static UsuarioListarDto toListarDto(Usuario usuario) {
    return new UsuarioListarDto(usuario.getId(), usuario.getNome(), usuario.getEmail());
  }
}
