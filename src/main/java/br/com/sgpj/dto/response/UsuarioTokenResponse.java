 package br.com.sgpj.dto.response;

 import java.util.List;

 public record UsuarioTokenResponse(
         Long id,
         String nome,
         String email,
         String token,
         String refreshToken,
         UsuarioSessaoResponse.CargoDto cargo,
         List<String> permissoes
 ) {
     public UsuarioSessaoResponse sessao() {
         return new UsuarioSessaoResponse(id, nome, email, cargo, permissoes);
     }
 }
