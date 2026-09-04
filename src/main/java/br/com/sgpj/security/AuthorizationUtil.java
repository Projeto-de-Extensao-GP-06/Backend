package br.com.sgpj.security;

import br.com.sgpj.adapter.security.UsuarioAutenticacaoAdapter;
import br.com.sgpj.entity.Cliente;
import br.com.sgpj.entity.Processo.ProcessoApp;
import br.com.sgpj.entity.Usuario;
import br.com.sgpj.exception.AcessoNegadoException;
import br.com.sgpj.repository.ClienteRepository;
import br.com.sgpj.repository.ProcessoAppRepository;
import br.com.sgpj.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationUtil {

    private final UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProcessoAppRepository processoAppRepository;

    public AuthorizationUtil(UsuarioAutenticacaoAdapter usuarioAutenticacaoAdapter,
                             UsuarioRepository usuarioRepository,
                             ClienteRepository clienteRepository,
                             ProcessoAppRepository processoAppRepository) {
        this.usuarioAutenticacaoAdapter = usuarioAutenticacaoAdapter;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.processoAppRepository = processoAppRepository;
    }

    public Usuario getUsuarioLogado() {
        String email = usuarioAutenticacaoAdapter.obterEmailUsuarioLogado();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new AcessoNegadoException("Usuário não encontrado"));
    }

    public void verificarAcessoCliente(Long clienteId) {
        Usuario usuario = getUsuarioLogado();
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new AcessoNegadoException("Cliente não encontrado"));

        if (!usuarioHasAccessToCliente(usuario, cliente)) {
            throw new AcessoNegadoException("Acesso negado ao cliente");
        }
    }

    public void verificarAcessoProcesso(Long processoId) {
        Usuario usuario = getUsuarioLogado();
        ProcessoApp processo = processoAppRepository.findById(processoId)
                .orElseThrow(() -> new AcessoNegadoException("Processo não encontrado"));

        if (!usuarioHasAccessToProcesso(usuario, processo)) {
            throw new AcessoNegadoException("Acesso negado ao processo");
        }
    }

    private boolean usuarioHasAccessToCliente(Usuario usuario, Cliente cliente) {
        return true;
    }

    private boolean usuarioHasAccessToProcesso(Usuario usuario, ProcessoApp processo) {
        if (processo.getUsuarioResponsavel() != null && 
            processo.getUsuarioResponsavel().getId().equals(usuario.getId())) {
            return true;
        }
        return false;
    }
}
