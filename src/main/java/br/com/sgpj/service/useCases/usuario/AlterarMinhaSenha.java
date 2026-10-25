package br.com.sgpj.service.useCases.usuario;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.dto.request.UsuarioAlterarSenhaRequest;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.PasswordConflictException;
import br.com.sgpj.exception.UsuarioNaoEncontradoException;
import br.com.sgpj.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Slf4j
@Service
public class AlterarMinhaSenha {

    private static final Pattern MAIUSCULA = Pattern.compile("[A-Z]");
    private static final Pattern MINUSCULA = Pattern.compile("[a-z]");
    private static final Pattern NUMERO = Pattern.compile("[0-9]");
    private static final Pattern ESPECIAL = Pattern.compile("[^A-Za-z0-9]");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final PasswordEncoder passwordEncoder;

    public AlterarMinhaSenha(
            UsuarioRepository usuarioRepository,
            UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
        this.passwordEncoder = passwordEncoder;
    }

    public void executar(UsuarioAlterarSenhaRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Insira um payload válido");
        }

        String email = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario == null) {
            log.error("Falha ao alterar senha. Usuário logado não encontrado para o email {}.", email);
            throw new UsuarioNaoEncontradoException(email);
        }

        if (usuario.getSenha() == null || !passwordEncoder.matches(request.senhaAtual(), usuario.getSenha())) {
            throw new IllegalArgumentException("A senha atual informada está incorreta.");
        }

        if (passwordEncoder.matches(request.novaSenha(), usuario.getSenha())) {
            throw new PasswordConflictException("A nova senha não pode ser igual à senha atual.");
        }

        validarRegrasSenha(request.novaSenha());

        String senhaCriptografada = passwordEncoder.encode(request.novaSenha());
        usuario.setSenha(senhaCriptografada);
        usuarioRepository.save(usuario);

        log.info("Senha alterada com sucesso pelo usuário ID {}", usuario.getId());
    }

    private void validarRegrasSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Insira uma senha válida");
        }
        if (senha.length() < 8) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 8 caracteres.");
        }
        if (senha.length() > 255) {
            throw new IllegalArgumentException("A senha deve conter no máximo 255 caracteres.");
        }
        if (!MAIUSCULA.matcher(senha).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 1 letra maiúscula.");
        }
        if (!MINUSCULA.matcher(senha).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 1 letra minúscula.");
        }
        if (!NUMERO.matcher(senha).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 1 número.");
        }
        if (!ESPECIAL.matcher(senha).find()) {
            throw new IllegalArgumentException("A senha deve conter pelo menos 1 caractere especial.");
        }
    }
}
