package br.com.sgpj.adapter.exemplo;

import br.com.sgpj.entity.Cargo;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.CargoNaoEncontradoException;
import br.com.sgpj.repository.CargoRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Adapter de exemplo para servir como modelo do time.
 *
 * <p>Objetivo: mostrar o formato mais simples para encapsular acesso a infra
 * (repositórios, gateways, clients etc.) sem criar camada de ports/interfaces.</p>
 *
 * <p>Regras práticas para novos adapters:</p>
 * <ul>
 *   <li>Receba dependências de infra no construtor.</li>
 *   <li>Mantenha métodos pequenos e com nome de caso de uso.</li>
 *   <li>Faça apenas orquestração de acesso externo (sem regra de negócio complexa).</li>
 *   <li>Lance exceções de domínio quando o dado obrigatório não existir.</li>
 * </ul>
 */
@Component
public class UsuarioAdapterExemplo {

    // Dependências de infraestrutura injetadas pelo Spring.
    private final UsuarioRepository usuarioRepository;
    private final CargoRepository cargoRepository;

    public UsuarioAdapterExemplo(UsuarioRepository usuarioRepository, CargoRepository cargoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.cargoRepository = cargoRepository;
    }

    /**
     * Exemplo de escrita no banco em um adapter.
     *
     * <p>O método busca o cargo e salva o usuário já montado. A lógica de negócio
     * (validações de senha, autorização, fluxo transacional complexo etc.) deve
     * ficar no Service, não no adapter.</p>
     */
    public Usuario criarUsuarioExemplo(String nome, String email, String senha, Long cargoId) {
        Cargo cargo = buscarCargoObrigatorio(cargoId);

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setSenha(senha);
        novoUsuario.setCargo(cargo);
        novoUsuario.setCriadoEm(LocalDateTime.now());
        novoUsuario.setAtivo(true);

        return usuarioRepository.save(novoUsuario);
    }

    /**
     * Exemplo de leitura no banco em um adapter.
     *
     * <p>Reaproveita query do repositório e faz um filtro simples em memória
     * somente para demonstração.</p>
     */
    public List<Usuario> listarUsuariosAtivosPorFuncaoExemplo(String funcao) {
        return usuarioRepository.findByCargo_NomeIgnoreCase(funcao.trim())
                .stream()
                .filter(Usuario::isAtivo)
                .toList();
    }

    /**
     * Método privado para centralizar busca obrigatória de cargo.
     */
    private Cargo buscarCargoObrigatorio(Long cargoId) {
        return cargoRepository.findById(cargoId)
                .orElseThrow(() -> new CargoNaoEncontradoException("Cargo não encontrado para esse ID: " + cargoId));
    }
}

