package br.com.sgpj.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import br.com.sgpj.entity.Usuario;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UsuarioDetalhesResponse implements UserDetails {

  private final Long id;

  private final String nome;

  private final String email;

  private final String senha;

  private final Collection<? extends GrantedAuthority> authorities;
  private final Boolean ativo;

  public UsuarioDetalhesResponse(Usuario usuario) {
    this.id = usuario.getId();
    this.nome = usuario.getNome();
    this.email = usuario.getEmail();
    this.senha = usuario.getSenha();
    this.ativo = usuario.isAtivo();
    this.authorities = loadAuthorities(usuario);
  }

  /**
   * Carrega as autoridades (permissões) do usuário baseado em seu cargo.
   *
   * <p>Se o usuário tiver um cargo associado, suas permissões são carregadas
   * a partir da tabela cargo_permissao. Caso contrário, retorna uma coleção vazia.</p>
   *
   * @param usuario entidade de usuário com cargo associado
   * @return coleção de autoridades (roles/permissions) do usuário
   */
  private Collection<? extends GrantedAuthority> loadAuthorities(Usuario usuario) {
    Set<GrantedAuthority> authorities = new HashSet<>();

    // Adiciona o cargo como uma authority com prefixo ROLE_
    if (usuario.getCargo() != null) {
      String cargoAuthority = "ROLE_" + usuario.getCargo().getNome().toUpperCase().replace(" ", "_");
      authorities.add(new SimpleGrantedAuthority(cargoAuthority));

      // Adiciona todas as permissões associadas ao cargo
      if (usuario.getCargo().getPermissoes() != null) {
        usuario.getCargo().getPermissoes().forEach(cargoPermissao -> {
          if (cargoPermissao.getPermissao() != null) {
            String permissaoAuthority = cargoPermissao.getPermissao().getNome().toUpperCase();
            authorities.add(new SimpleGrantedAuthority(permissaoAuthority));
          }
        });
      }
    }

    return authorities;
  }

  public Long getId() {
    return id;
  }

  public String getNome() {
    return nome;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return senha;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return Boolean.TRUE.equals(ativo);
  }
}
